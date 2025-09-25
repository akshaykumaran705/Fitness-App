package com.fitness.activityservice.controller;

import com.fitness.activityservice.dto.ActivityStatusResponse;
import com.fitness.activityservice.dto.SensorDataRequest;
import com.fitness.activityservice.dto.SensorDataResponse;
import com.fitness.activityservice.service.HarService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/activities/")
@CrossOrigin(origins = "http://localhost:3000/",methods = {RequestMethod.GET, RequestMethod.POST})
public class ActivityController {
//    private ActivityService activityService;
    @Autowired
    private HarService harService;
////    @PostMapping
////    public ResponseEntity<SensorDataResponse> trackActivity(@RequestBody SensorDataRequest){
////      return ResponseEntity.ok(activityService.trackActivity(request));
//    }
    @PostMapping("/sensordata")
    public ResponseEntity<Void> receiveSensorData(@RequestBody SensorDataRequest data){
        harService.processIncomingData(data.getSensorReadings());
        return ResponseEntity.accepted().build();
    }

    @GetMapping("/status")

    public ResponseEntity<ActivityStatusResponse> getActivityStatus(){
        return ResponseEntity.ok(harService.getLatestActivityStatus());
    }
}
