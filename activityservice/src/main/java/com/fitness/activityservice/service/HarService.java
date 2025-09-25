package com.fitness.activityservice.service;

import com.fitness.activityservice.dto.ActivityStatusResponse;
import com.fitness.activityservice.dto.SensorDataResponse;
import jakarta.annotation.PostConstruct;
import ml.dmlc.xgboost4j.java.Booster;
import ml.dmlc.xgboost4j.java.DMatrix;
import ml.dmlc.xgboost4j.java.XGBoost;
import ml.dmlc.xgboost4j.java.XGBoostError;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.aggregation.PrefixingDelegatingAggregationOperationContext;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.atomic.AtomicReference;

@Service
public class HarService {
    private final Deque<SensorDataResponse> dataBuffer = new ConcurrentLinkedDeque<>();
   // @Autowired
    private Booster xgbModel;
    private final AtomicReference<ActivityStatusResponse> latestStatus = new AtomicReference<>();
    private final Object bufferLock = new Object();
    private static final int SAMPLING_RATE_HZ = 50;
    private static final double WINDOW_SIZE_SECONDS = 2.56;
    private static final int WINDOW_SIZE_SAMPLES = (int)(SAMPLING_RATE_HZ * WINDOW_SIZE_SECONDS);
    private static final int WINDOW_OVERLAP_SAMPLES  =WINDOW_SIZE_SAMPLES/2;
    private final FeatureExtractionService featureExtractionService;

    @PostConstruct
    public void loadModel(){
        try{
            InputStream modelStream = getClass().getClassLoader().getResourceAsStream("activity_model.bst");
            if(modelStream == null){
                throw new IllegalStateException("model resource not found");
            }
            this.xgbModel = XGBoost.loadModel(modelStream);
            System.out.println("XGBoost model loaded successfully");
        } catch (Exception e){
            e.printStackTrace();
            throw new IllegalStateException("failed to load model",e);
        }
    }
    @Autowired
    public HarService(FeatureExtractionService featureExtractionService,RagRecommendationService ragRecommendationService) {
        this.featureExtractionService = featureExtractionService;
        this.ragRecommendationService = ragRecommendationService;
    }

    public void processIncomingData(List<SensorDataResponse> sensorReadings) {
        dataBuffer.addAll(sensorReadings);
        synchronized (bufferLock) {
            while (dataBuffer.size() >= WINDOW_OVERLAP_SAMPLES) {
                processWindow();
            }
        }
    }
    private RagRecommendationService ragRecommendationService;
    private void processWindow() {
        System.out.println("Processing a window of" + WINDOW_SIZE_SAMPLES + "samples");
        List<SensorDataResponse> window = extractWindowFromBuffer();
        if (window == null) {
            return;
        }
        float[] featureVector = featureExtractionService.extractFeatures(window);
        System.out.println("Extracted features: " + Arrays.toString(featureVector));
        String predictedActivity = predictActivity(featureVector);
        System.out.println("Predicted activity: " + predictedActivity);
        if (!predictedActivity.equals("UNKNOWN")) {
            String recommendation = ragRecommendationService.getRecommendation(predictedActivity);
            System.out.println("AI Recommendation:" + recommendation);

            ActivityStatusResponse newStatus = new ActivityStatusResponse();
            newStatus.setLastDetectedActivity(predictedActivity);
            newStatus.setLatestRecommendation(recommendation);
            newStatus.setTimestamp(System.currentTimeMillis());
            latestStatus.set(newStatus);
        }
        for(int i=0;i<WINDOW_OVERLAP_SAMPLES+1;i++) {
            dataBuffer.poll();

        }

    }
    public ActivityStatusResponse getLatestActivityStatus(){
        return latestStatus.get() != null ? latestStatus.get(): createDefaultStatus();
    }
    private String predictActivity(float[] featureVector) {
        if(xgbModel == null){
            return "Model Not Loaded";
        }
        try{
            DMatrix dMatrix = new DMatrix(featureVector,1, featureVector.length);
            float[][] prediction = xgbModel.predict(dMatrix);
            return mapPredictionToLabel(prediction[0][0]);
        } catch (XGBoostError e){
            e.printStackTrace();
            return "Prediction Error";
        }
    }
    private String mapPredictionToLabel(float prediction) {
        int activityClass = (int) prediction;
        switch (activityClass){
            case 0: return "WALKING";
            case 1: return "SITTING";
            case 2: return "STANDING";
            case 3: return "RUNNING";
            default: return "UNKNOWN";
        }
    }
    private List<SensorDataResponse> extractWindowFromBuffer() {
        if(dataBuffer.size()<WINDOW_OVERLAP_SAMPLES){
            return null;
        }
        return new ArrayList<>(dataBuffer).subList(0,WINDOW_SIZE_SAMPLES);
    }
    private ActivityStatusResponse createDefaultStatus(){
        ActivityStatusResponse defaultStatus = new ActivityStatusResponse();
        defaultStatus.setLastDetectedActivity("Awaiting data");
        defaultStatus.setLatestRecommendation("No recommendations yet. Start sending sensor data");
        defaultStatus.setTimestamp(System.currentTimeMillis());
        return defaultStatus;
    }
}
