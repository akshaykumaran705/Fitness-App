package com.fitness.userservice.controller;

import com.fitness.userservice.dto.CreateGoalRequest;
import com.fitness.userservice.model.Goal;
import com.fitness.userservice.model.GoalStatus;
import com.fitness.userservice.repository.GoalRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.oauth2.resource.OAuth2ResourceServerProperties;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/goal")
@RequiredArgsConstructor
public class GoalController {

    private final GoalRepository goalRepository;
    @PostMapping
    public ResponseEntity<Goal> createGoal(@AuthenticationPrincipal Jwt jwt, @RequestBody CreateGoalRequest request) {
        String userId = jwt.getSubject();

        // FIX: Build a new Goal entity from the safe DTO
        Goal newGoal = Goal.builder()
                .userId(userId)
                .title(request.getTitle())
                .goalType(request.getGoalType())
                .targetValue(request.getTargetValue())
                .currentValue(0) // Start progress at 0
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .status(GoalStatus.COMPLETED)
                .build();

        Goal savedGoal = goalRepository.save(newGoal);
        return ResponseEntity.ok(savedGoal);
    }
@GetMapping("/{userId}")
    public ResponseEntity<List<Goal>> getMyGoals(@AuthenticationPrincipal Jwt jwt,@PathVariable String userId){
    return ResponseEntity.ok(goalRepository.findByUserId(userId));
}
@DeleteMapping("/{goalid}")
    public ResponseEntity<List<Goal>> deleteGoal(@AuthenticationPrincipal Jwt jwt, @PathVariable String goalid){
    String userId = jwt.getSubject();
    Optional<Goal> goal = goalRepository.findByIdAndUserId(goalid,userId);
    if(goal.isPresent()){
        goalRepository.deleteById(goalid);
        return ResponseEntity.noContent().build();
    }
else {
    return ResponseEntity.notFound().build();
    }
}
}

