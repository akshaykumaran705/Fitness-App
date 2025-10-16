package com.fitness.activityservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fitness.activityservice.dto.*;
import com.fitness.activityservice.model.*;
import com.fitness.activityservice.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;   
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/activities")
public class ActivityController {
    
    @Autowired
    private HarService harService;
    
    @Autowired
    private ActivityService activityService;
    
    @Autowired
    private SensorProcessingService sensorProcessingService;
    
    /**
     * MAIN ENDPOINT FOR SENSOR LOGGER APP
     */
    @PostMapping("/sensor/stream")
    public ResponseEntity<Map<String, Object>> streamSensorData(
            @AuthenticationPrincipal Jwt jwt,
            @RequestBody SensorLoggerMessage message) {
    
        String userId = jwt.getSubject();
    
        try {
            if (message.getPayload() == null || message.getPayload().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                    "status", "error",
                    "message", "Payload is empty or missing"
                ));
            }
    
            // Process sensor message
            sensorProcessingService.processSensorLoggerMessage(userId, message);
    
            return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "✅ Sensor data processed successfully",
                "userId", userId,
                "messageId", message.getMessageId(),
                "sessionId", message.getSessionId(),
                "deviceId", message.getDeviceId(),
                "processedReadings", message.getPayload().size()
            ));
        } catch (Exception e) {
            log.error("❌ Error processing sensor data for user {}: {}", userId, e.getMessage(), e);
            return ResponseEntity.internalServerError().body(Map.of(
                "status", "error",
                "message", "Failed to process sensor data",
                "error", e.getMessage()
            ));
        }
    }
    
    /**
     * TEST ENDPOINT (No authentication)
     */
    @PostMapping("sensor/stream/test")
    public ResponseEntity<Map<String, Object>> streamSensorDataTest(@RequestBody Object body) {

        int processedCount = handleSensorMessages("qrkh9p9IGFRDQm9uGLIOY03dhik1", body);

        return ResponseEntity.ok(Map.of(
            "status", "success",
            "message", "✅ Data processed successfully",
            "processedMessages", processedCount
        ));
    }

    /**
     * Internal helper – automatically handles both single object and array payloads
     */
    
    
    /**
     * Get sensor sessions
     */
    @GetMapping("/sensor/sessions")
    public ResponseEntity<List<SensorSession>> getSensorSessions(
            @AuthenticationPrincipal Jwt jwt) {
        
        String userId = jwt.getSubject();
        return ResponseEntity.ok(sensorProcessingService.getUserSessions(userId));
    }
    
    /**
     * End sensor session
     */
    @PostMapping("/sensor/session/{sessionId}/end")
    public ResponseEntity<SensorSession> endSensorSession(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable String sessionId) {
        
        String userId = jwt.getSubject();
        SensorSession session = sensorProcessingService.endSession(userId, sessionId);
        return ResponseEntity.ok(session);
    }
    
    /**
     * Send sensor data (custom format for backwards compatibility)
     */
    @PostMapping("/sensordata")
    public ResponseEntity<Void> receiveSensorData(
            @AuthenticationPrincipal Jwt jwt, 
            @RequestBody SensorDataRequest data) {
        
        String userId = jwt.getSubject();
        harService.processIncomingData(userId, data.getSensorReadings());
        return ResponseEntity.accepted().build();
    }
    
    /**
     * Get activity status
     */
    @GetMapping("/status")
    public ResponseEntity<ActivityStatusResponse> getActivityStatus(
            @AuthenticationPrincipal Jwt jwt) {
        
        String userId = jwt.getSubject();
        return ResponseEntity.ok(harService.getLatestActivityStatus(userId));
    }
    
    /**
     * Get activity history - FIXED: Now returns PageResponse DTO
     */
    @GetMapping("/history/{userId}")
    public ResponseEntity<PageResponse<ActivityResponse>> getActivityHistory(
            @PathVariable String userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        PageResponse<ActivityResponse> response = activityService.getActivityHistory(userId, page, size);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Log workout
     */
    @PostMapping("/workout")
    public ResponseEntity<WorkoutSession> logWorkout(
            @AuthenticationPrincipal Jwt jwt, 
            @RequestBody WorkoutSessionRequest request) {
        
        String userId = jwt.getSubject();
        WorkoutSession savedSession = activityService.saveWorkoutSession(userId, request);
        return ResponseEntity.ok(savedSession);
    }
    
    /**
     * Get workout history
     */
    @GetMapping("/workout/{userId}")
    public ResponseEntity<List<WorkoutSession>> getWorkoutHistory(
            @PathVariable String userId) {
        
        return ResponseEntity.ok(activityService.getWorkoutHistory(userId));
    }
    private int handleSensorMessages(String userId, Object body) {
        ObjectMapper mapper = new ObjectMapper();
        int processed = 0;

        if (body instanceof Map) {
            // Single message
            SensorLoggerMessage message = mapper.convertValue(body, SensorLoggerMessage.class);
            sensorProcessingService.processSensorLoggerMessage(userId, message);
            processed = 1;

        } else if (body instanceof List) {
            // Multiple messages
            List<?> list = (List<?>) body;
            for (Object obj : list) {
                SensorLoggerMessage message = mapper.convertValue(obj, SensorLoggerMessage.class);
                sensorProcessingService.processSensorLoggerMessage(userId, message);
                processed++;
            }

        } else {
            System.err.println("⚠️ Unexpected request body type: " + body.getClass());
        }

        return processed;
    }
}