package com.fitness.activityservice.service;

import com.fitness.activityservice.dto.SensorDataResponse;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.function.Function;
@Service
public class FeatureExtractionService {
    public float[] extractFeatures(List<SensorDataResponse> window){
        double meanAccX = calculateMean(window, SensorDataResponse::getAcc_x);
        double meanAccY = calculateMean(window, SensorDataResponse::getAcc_y);
        double meanAccZ = calculateMean(window, SensorDataResponse::getAcc_z);

        double meanGyroX = calculateMean(window, SensorDataResponse::getGyro_x);
        double meanGyroY = calculateMean(window, SensorDataResponse::getGyro_y);
        double meanGyroZ = calculateMean(window, SensorDataResponse::getGyro_z);

        return  new float[]{
                (float) meanAccX,(float) meanAccY,(float) meanAccZ,(float) meanAccY,(float) meanAccZ,
                (float) meanGyroX,(float) meanGyroY, (float) meanGyroZ
        };
    }
    private double calculateMean(List<SensorDataResponse> window, Function<SensorDataResponse, Double> mapper){
        DescriptiveStatistics stats = new DescriptiveStatistics();
        window.stream()
                .mapToDouble(mapper::apply)
                .forEach(stats::addValue);
        return stats.getMean();
    }
}
