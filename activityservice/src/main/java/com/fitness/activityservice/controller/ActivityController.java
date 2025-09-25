package com.fitness.activityservice.controller;

import com.fitness.activityservice.dto.ActivityStatusResponse;
import com.fitness.activityservice.dto.SensorDataRequest;
import com.fitness.activityservice.dto.SensorDataResponse;
import com.fitness.activityservice.service.HarService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.oauth2.resource.OAuth2ResourceServerProperties;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import com.fitness.activityservice.config.ActivityServiceCorsConfig;
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
    @PostMapping("/sensordata")
    public ResponseEntity<Void> receiveSensorData(@AuthenticationPrincipal Jwt jwt,@RequestBody SensorDataRequest data){
        String userId = jwt.getStubject();
        harService.processIncomingData(userId,data.getSensorReadings());
        return ResponseEntity.accepted().build();
    }
    @GetMapping("/status")
    public ResponseEntity<ActivityStatusResponse> getActivityStatus(@AuthenticationPrincipal OAuth2ResourceServerProperties.Jwt jwt)
    {
        String userId = jwt.getSubject();
        return ResponseEntity.ok(harService.getLatestActivityStatus(userId));
    }
}
