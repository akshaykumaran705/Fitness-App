package com.fitness.activityservice.service;

import com.fitness.activityservice.model.ProcessedReading;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class StepCounterService {
    
    private static final double STEP_THRESHOLD = 10.5;
    private static final long MIN_STEP_INTERVAL_MS = 200;
    
    private Long lastStepTime = null;
    
    public int countSteps(List<ProcessedReading> readings) {
        int steps = 0;
        
        for (int i = 1; i < readings.size() - 1; i++) {
            ProcessedReading curr = readings.get(i);
            ProcessedReading prev = readings.get(i - 1);
            ProcessedReading next = readings.get(i + 1);
            
            if (curr.getAccMagnitude() == null) continue;
            
            boolean isPeak = curr.getAccMagnitude() > STEP_THRESHOLD &&
                           curr.getAccMagnitude() > prev.getAccMagnitude() &&
                           curr.getAccMagnitude() > next.getAccMagnitude();
            
            if (isPeak) {
                if (lastStepTime == null || 
                    (curr.getTimestamp() - lastStepTime) >= MIN_STEP_INTERVAL_MS) {
                    steps++;
                    curr.setIsStep(true);
                    lastStepTime = curr.getTimestamp();
                }
            }
        }
        
        return steps;
    }
    
    public void reset() {
        lastStepTime = null;
    }
}
