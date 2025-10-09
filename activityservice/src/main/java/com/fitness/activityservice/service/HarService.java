package com.fitness.activityservice.service;

import com.fitness.activityservice.dto.ActivityStatusResponse;
import com.fitness.activityservice.dto.SensorDataResponse;
import com.fitness.activityservice.model.Activity;
import com.fitness.activityservice.model.ActivityType;
import com.fitness.activityservice.repository.ActivityRepository;
import jakarta.annotation.PostConstruct;
import ml.dmlc.xgboost4j.java.Booster;
import ml.dmlc.xgboost4j.java.DMatrix;
import ml.dmlc.xgboost4j.java.XGBoost;
import ml.dmlc.xgboost4j.java.XGBoostError;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;

@Service
public class HarService {

    private static final Logger logger = LoggerFactory.getLogger(HarService.class);

    private final Map<String, Deque<SensorDataResponse>> userBuffers = new ConcurrentHashMap<>();
    private final Map<String, ActivityStatusResponse> latestStatusStore = new ConcurrentHashMap<>();
    private final Map<String, UserActivityState> userStates = new ConcurrentHashMap<>();

    private final ActivityRepository activityRepository;
    private Booster xgbModel;
    private final FeatureExtractionService featureExtractionService;
    private final RagRecommendationService ragRecommendationService;

    private final Object bufferLock = new Object();

    private static final int SAMPLING_RATE_HZ = 50;
    private static final double WINDOW_SIZE_SECONDS = 2.56;
    private static final int WINDOW_SIZE_SAMPLES = (int) (SAMPLING_RATE_HZ * WINDOW_SIZE_SECONDS);
    private static final int WINDOW_OVERLAP_SAMPLES = WINDOW_SIZE_SAMPLES / 2;
    private static final long MIN_DURATION_TO_SAVE_SECONDS = 2; // Increased for better stability

    @Autowired
    public HarService(FeatureExtractionService featureExtractionService, RagRecommendationService ragRecommendationService, ActivityRepository activityRepository) {
        this.featureExtractionService = featureExtractionService;
        this.ragRecommendationService = ragRecommendationService;
        this.activityRepository = activityRepository;
    }

    @PostConstruct
    public void loadModel() {
        try (InputStream modelStream = getClass().getClassLoader().getResourceAsStream("activity_model.bst")) {
            if (modelStream == null) throw new IllegalStateException("Model resource 'activity_model.bst' not found!");
            this.xgbModel = XGBoost.loadModel(modelStream);
            logger.info("XGBoost model loaded successfully.");
        } catch (Exception e) {
            logger.error("Failed to load XGBoost model", e);
            throw new RuntimeException("Failed to load XGBoost model", e);
        }
    }

    @Async
    public void processIncomingData(String userId, List<SensorDataResponse> sensorReadings) {
        if (sensorReadings == null || sensorReadings.isEmpty()) {
            logger.warn("Received empty sensor readings for user {}.", userId);
            return;
        }

        Deque<SensorDataResponse> userBuffer = userBuffers.computeIfAbsent(userId, k -> new ConcurrentLinkedDeque<>());
        userBuffer.addAll(sensorReadings);

        synchronized (bufferLock) {
            while (userBuffer.size() >= WINDOW_SIZE_SAMPLES) {
                processWindowForUser(userId, userBuffer);
            }
        }
    }

    private void processWindowForUser(String userId, Deque<SensorDataResponse> userBuffer) {
        List<SensorDataResponse> window = new ArrayList<>(userBuffer).subList(0, WINDOW_SIZE_SAMPLES);

        float[] featureVector = featureExtractionService.extractFeatures(window);
        String predictedActivity = predictActivity(featureVector);

        logger.info("Predicted activity for user {}: {}", userId, predictedActivity);
        updateUserActivityState(userId, predictedActivity);

        if (!predictedActivity.equals("UNKNOWN")) {
            String recommendation = ragRecommendationService.getRecommendation(predictedActivity);
            ActivityStatusResponse newStatus = new ActivityStatusResponse();
            newStatus.setLastDetectedActivity(predictedActivity);
            newStatus.setLatestRecommendation(recommendation);
            newStatus.setTimestamp(System.currentTimeMillis());
            latestStatusStore.put(userId, newStatus);
        }

        for (int i = 0; i < WINDOW_OVERLAP_SAMPLES; i++) {
            if (!userBuffer.isEmpty()) {
                userBuffer.poll();
            }
        }
    }

    private void updateUserActivityState(String userId, String newActivity) {
        if (newActivity.equals("UNKNOWN")) return;

        UserActivityState currentState = userStates.computeIfAbsent(userId, k -> {
            logger.info("First detection for user {}: {}. Starting tracking.", userId, newActivity);
            return new UserActivityState(newActivity);
        });

        // --- THE CRITICAL FIX IS HERE ---
        // The "!" inverts the logic to correctly trigger on an activity CHANGE.
        if (!currentState.getCurrentActivity().equals(newActivity)) {
            logger.info("Activity CHANGED for user {}: from {} to {}", userId, currentState.getCurrentActivity(), newActivity);

            long durationSeconds = Duration.between(currentState.getStartTime(), LocalDateTime.now()).getSeconds();

            if (durationSeconds >= MIN_DURATION_TO_SAVE_SECONDS) {
                logger.info("Duration ({}) is long enough. SAVING previous activity: {}", durationSeconds, currentState.getCurrentActivity());
                saveActivity(userId, currentState.getCurrentActivity(), currentState.getStartTime(), durationSeconds);
            } else {
                logger.warn("Duration ({}) was too short. Not saving previous activity: {}", durationSeconds, currentState.getCurrentActivity());
            }

            // Reset the state to begin tracking the new activity.
            currentState.reset(newActivity);
        }
    }

    private void saveActivity(String userId, String activityName, LocalDateTime startTime, long durationSeconds) {
        try {
            Activity activityToSave = Activity.builder()
                    .userId(userId)
                    .type(ActivityType.valueOf(activityName))
                    .startTime(startTime)
                    .duration((int) durationSeconds)
                    .caloriesBurned(calculateCaloriesBurned(ActivityType.valueOf(activityName), (int) durationSeconds))
                    .build();

            Activity savedActivity = activityRepository.save(activityToSave);
            logger.info("Successfully saved activity with ID: {} for user: {}", savedActivity.getId(), userId);
        } catch (Exception e) {
            logger.error("Failed to save activity for user {}: {}", userId, e.getMessage(), e);
        }
    }

    public ActivityStatusResponse getLatestActivityStatus(String userId) {
        return latestStatusStore.getOrDefault(userId, createDefaultStatus(userId));
    }

    private String predictActivity(float[] featureVector) {
        if (xgbModel == null) return "MODEL_NOT_LOADED";
        try {
            DMatrix dMatrix = new DMatrix(featureVector, 1, 10); // Using 10 features
            float[][] prediction = xgbModel.predict(dMatrix);
            return mapPredictionToLabel(prediction[0][0]);
        } catch (XGBoostError e) { e.printStackTrace(); return "PREDICTION_ERROR"; }
    }

    private String mapPredictionToLabel(float prediction) {
        int activityClass = Math.round(prediction);
        switch (activityClass) {
            case 0: return "WALKING";
            case 1: return "SITTING";
            case 2: return "STANDING";
            case 3: return "RUNNING";
            default: return "UNKNOWN";
        }
    }

    private ActivityStatusResponse createDefaultStatus(String userId) {
        ActivityStatusResponse defaultStatus = new ActivityStatusResponse();
        defaultStatus.setLastDetectedActivity("UNKNOWN");
        defaultStatus.setLatestRecommendation("No recommendations yet. Start sending sensor data.");
        defaultStatus.setTimestamp(System.currentTimeMillis());
        return defaultStatus;
    }

    private int calculateCaloriesBurned(ActivityType activityType, int durationSeconds) {
        double caloriesPerMinute;
        switch (activityType) {
            case RUNNING: caloriesPerMinute = 10.0; break;
            case WALKING: caloriesPerMinute = 4.0; break;
            case SITTING: caloriesPerMinute = 1.0; break;
            case STANDING: caloriesPerMinute = 1.2; break;
            default: caloriesPerMinute = 2.0; break;
        }
        return (int) (caloriesPerMinute * durationSeconds / 60.0);
    }

    private static class UserActivityState {
        private String currentActivity;
        private LocalDateTime startTime;

        public UserActivityState(String activity) { this.currentActivity = activity; this.startTime = LocalDateTime.now(); }
        public void reset(String newActivity) { this.currentActivity = newActivity; this.startTime = LocalDateTime.now(); }
        public String getCurrentActivity() { return currentActivity; }
        public LocalDateTime getStartTime() { return startTime; }
    }
}