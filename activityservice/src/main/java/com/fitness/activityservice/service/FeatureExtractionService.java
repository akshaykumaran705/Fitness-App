package com.fitness.activityservice.service;

import com.fitness.activityservice.dto.SensorDataResponse;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.function.Function;

@Service
public class FeatureExtractionService {

    /**
     * Extracts 10 features from a window of sensor data:
     * - 6 mean values (acc_x, acc_y, acc_z, gyro_x, gyro_y, gyro_z)
     * - 4 standard deviation values (acc_x, acc_y, acc_z, gyro_x)
     */
    public float[] extractFeatures(List<SensorDataResponse> window) {
        // Calculate means for all 6 sensor axes
        double meanAccX = calculateMean(window, SensorDataResponse::getAcc_x);
        double meanAccY = calculateMean(window, SensorDataResponse::getAcc_y);
        double meanAccZ = calculateMean(window, SensorDataResponse::getAcc_z);
        double meanGyroX = calculateMean(window, SensorDataResponse::getGyro_x);
        double meanGyroY = calculateMean(window, SensorDataResponse::getGyro_y);
        double meanGyroZ = calculateMean(window, SensorDataResponse::getGyro_z);

        // Calculate standard deviations for 4 key axes
        double stdAccX = calculateStdDev(window, SensorDataResponse::getAcc_x);
        double stdAccY = calculateStdDev(window, SensorDataResponse::getAcc_y);
        double stdAccZ = calculateStdDev(window, SensorDataResponse::getAcc_z);
        double stdGyroX = calculateStdDev(window, SensorDataResponse::getGyro_x);

        // Return exactly 10 features (no duplicates!)
        return new float[]{
                (float) meanAccX, (float) meanAccY, (float) meanAccZ,
                (float) meanGyroX, (float) meanGyroY, (float) meanGyroZ,
                (float) stdAccX, (float) stdAccY, (float) stdAccZ, (float) stdGyroX
        };
    }

    private double calculateMean(List<SensorDataResponse> window, Function<SensorDataResponse, Double> mapper) {
        DescriptiveStatistics stats = new DescriptiveStatistics();
        window.stream()
                .mapToDouble(mapper::apply)
                .forEach(stats::addValue);
        return stats.getMean();
    }

    private double calculateStdDev(List<SensorDataResponse> window, Function<SensorDataResponse, Double> mapper) {
        DescriptiveStatistics stats = new DescriptiveStatistics();
        window.stream()
                .mapToDouble(mapper::apply)
                .forEach(stats::addValue);
        return stats.getStandardDeviation();
    }

    // Keep these methods for potential future use
    private double calculateZeroCrossingRate(double[] data, double mean) {
        int crossings = 0;
        for (int i = 1; i < data.length; i++) {
            // Check if the signal crossed the mean value
            if ((data[i] - mean) * (data[i - 1] - mean) < 0) {
                crossings++;
            }
        }
        // Normalize by the number of possible crossings
        return (double) crossings / (data.length - 1);
    }

    private double calculateSMA(double[] accX, double[] accY, double[] accZ) {
        double sum = 0;
        for (int i = 0; i < accX.length; i++) {
            sum += Math.abs(accX[i]) + Math.abs(accY[i]) + Math.abs(accZ[i]);
        }
        return sum / accX.length;
    }
}
