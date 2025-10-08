package com.fitness.activityservice.controller;

import com.fitness.activityservice.dto.ActivityStatusResponse;
import com.fitness.activityservice.dto.SensorDataRequest;
import com.fitness.activityservice.dto.SensorDataResponse;
import com.fitness.activityservice.dto.WorkoutSessionRequest;
import com.fitness.activityservice.model.Activity;
import com.fitness.activityservice.model.WorkoutSession;
import com.fitness.activityservice.service.ActivityService;
import com.fitness.activityservice.service.HarService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import retrofit2.http.Path;

import java.util.ArrayList;
import java.util.List;
@RestController
@RequestMapping("/api/activities/")
//@CrossOrigin(origins = "http://localhost:3000",methods = {RequestMethod.GET, RequestMethod.OPTIONS})
public class ActivityController {
//    private ActivityService activityService;
    @Autowired
    private HarService harService;
////    @PostMapping
////    public ResponseEntity<SensorDataResponse> trackActivity(@RequestBody SensorDataRequest){
////      return ResponseEntity.ok(activityService.trackActivity(request));
//    }
@Autowired
private ActivityService activityService;
    @PostMapping("/sensordata")
    public ResponseEntity<Void> receiveSensorData(@AuthenticationPrincipal Jwt jwt, @RequestBody SensorDataRequest data){
        String userId = jwt.getSubject();
        harService.processIncomingData(userId, data.getSensorReadings());
        return ResponseEntity.accepted().build();
    }
    @GetMapping("/status")
    public ResponseEntity<ActivityStatusResponse> getActivityStatus(@AuthenticationPrincipal Jwt jwt)
    {
        String userId = jwt.getSubject();
        return ResponseEntity.ok(harService.getLatestActivityStatus(userId));
    }
    @GetMapping("/history/{userId}")
    public ResponseEntity<Page<Activity>> getActivityHistory(
        @PathVariable String userId,
    @RequestParam(defaultValue = "0") int page,
    @RequestParam(defaultValue = "20") int size){
    return ResponseEntity.ok(activityService.getActivityHistory(userId,page,size));
    }
    @PostMapping("/workout")
    public ResponseEntity<WorkoutSession> logWorkout(@AuthenticationPrincipal Jwt jwt, @RequestBody WorkoutSessionRequest request){
        String userId = jwt.getSubject();
        WorkoutSession savedSession = activityService.saveWorkoutSession(userId, request);
        return ResponseEntity.ok(savedSession);
    }
    @GetMapping("/workout/{userId}")
    public ResponseEntity<List<WorkoutSession>> getWorkoutHistory(@PathVariable String userId){
        return ResponseEntity.ok(activityService.getWorkoutHistory(userId));
    }

}
