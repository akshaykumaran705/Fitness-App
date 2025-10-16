import ml.dmlc.xgboost4j.java.Booster;
import ml.dmlc.xgboost4j.java.DMatrix;
import ml.dmlc.xgboost4j.java.XGBoost;
import ml.dmlc.xgboost4j.java.XGBoostError;

import java.io.File;
import java.io.FileInputStream;
import java.util.Arrays;

/**
 * Debug script to test your model with known inputs
 * This helps identify where the problem is:
 * 1. Model file issue
 * 2. Feature extraction issue
 * 3. Label mapping issue
 */
public class DebugPrediction {

    public static void main(String[] args) throws Exception {
        System.out.println("=================================================");
        System.out.println("Model Prediction Debug Tool");
        System.out.println("=================================================\n");

        // Load the model
        String modelPath = "./activity_model.bst";
        if (!new File(modelPath).exists()) {
            System.err.println("ERROR: Model file not found at: " + modelPath);
            System.err.println("Make sure you've trained the model first!");
            return;
        }

        Booster model = XGBoost.loadModel(modelPath);
        System.out.println("✓ Model loaded successfully\n");

        // Test with synthetic data for each activity
        System.out.println("=================================================");
        System.out.println("Testing Model with Synthetic Data");
        System.out.println("=================================================\n");

        testActivity(model, "SITTING", createSittingFeatures());
        testActivity(model, "STANDING", createStandingFeatures());
        testActivity(model, "WALKING", createWalkingFeatures());
        testActivity(model, "RUNNING", createRunningFeatures());

        System.out.println("\n=================================================");
        System.out.println("Feature Analysis");
        System.out.println("=================================================\n");

        System.out.println("Expected feature characteristics:");
        System.out.println("\nSITTING:");
        System.out.println("  - Very low acceleration variance (std < 0.1)");
        System.out.println("  - acc_z close to 9.8 (gravity)");
        System.out.println("  - Very low gyroscope values (< 0.05)");

        System.out.println("\nSTANDING:");
        System.out.println("  - Low acceleration variance (std < 0.2)");
        System.out.println("  - acc_z close to 9.8");
        System.out.println("  - Low gyroscope values (< 0.1)");

        System.out.println("\nWALKING:");
        System.out.println("  - Moderate acceleration variance (std 0.3-0.8)");
        System.out.println("  - Rhythmic pattern in acc_x, acc_y");
        System.out.println("  - Moderate gyroscope values (0.1-0.3)");

        System.out.println("\nRUNNING:");
        System.out.println("  - High acceleration variance (std > 0.8)");
        System.out.println("  - Strong rhythmic pattern");
        System.out.println("  - High gyroscope values (> 0.3)");

        System.out.println("\n=================================================");
        System.out.println("How to Fix Detection Issues");
        System.out.println("=================================================\n");

        System.out.println("If predictions are wrong:");
        System.out.println("1. Check if your training data has the right features");
        System.out.println("2. Make sure sensor values match expected ranges");
        System.out.println("3. Retrain with more diverse data");
        System.out.println("4. Check that Postman data matches these patterns\n");
    }

    private static void testActivity(Booster model, String activityName, float[] features)
            throws XGBoostError {
        System.out.println("Testing: " + activityName);
        System.out.println("Features: " + Arrays.toString(features));

        DMatrix dMatrix = new DMatrix(features, 1, features.length);
        float[][] prediction = model.predict(dMatrix);
        float rawPrediction = prediction[0][0];
        int predictedClass = Math.round(rawPrediction);

        String predictedLabel = mapPredictionToLabel(predictedClass);

        System.out.println("Raw prediction: " + rawPrediction);
        System.out.println("Predicted class: " + predictedClass);
        System.out.println("Predicted label: " + predictedLabel);

        if (predictedLabel.equals(activityName)) {
            System.out.println("✓ CORRECT\n");
        } else {
            System.out.println("✗ WRONG - Expected " + activityName + "\n");
        }
    }

    /**
     * Create realistic features for SITTING
     * Sitting characteristics:
     * - Very stable, minimal movement
     * - acc_z ≈ 9.8 (gravity)
     * - Very low std dev
     */
    private static float[] createSittingFeatures() {
        return new float[]{
                // Means: acc_x, acc_y, acc_z, gyro_x, gyro_y, gyro_z
                0.01f,   // mean_acc_x (almost no movement)
                0.02f,   // mean_acc_y
                9.8f,    // mean_acc_z (gravity)
                0.001f,  // mean_gyro_x (almost no rotation)
                0.002f,  // mean_gyro_y
                0.001f,  // mean_gyro_z
                // Std devs: acc_x, acc_y, acc_z, gyro_x
                0.02f,   // std_acc_x (very low variance)
                0.02f,   // std_acc_y
                0.05f,   // std_acc_z
                0.005f   // std_gyro_x
        };
    }

    /**
     * Create realistic features for STANDING
     */
    private static float[] createStandingFeatures() {
        return new float[]{
                0.05f,   // mean_acc_x (slight swaying)
                0.05f,   // mean_acc_y
                9.7f,    // mean_acc_z
                0.01f,   // mean_gyro_x
                0.01f,   // mean_gyro_y
                0.01f,   // mean_gyro_z
                0.08f,   // std_acc_x (low variance)
                0.08f,   // std_acc_y
                0.1f,    // std_acc_z
                0.02f    // std_gyro_x
        };
    }

    /**
     * Create realistic features for WALKING
     */
    private static float[] createWalkingFeatures() {
        return new float[]{
                0.5f,    // mean_acc_x (moderate movement)
                0.3f,    // mean_acc_y
                9.5f,    // mean_acc_z
                0.05f,   // mean_gyro_x
                0.05f,   // mean_gyro_y
                0.08f,   // mean_gyro_z
                0.4f,    // std_acc_x (moderate variance)
                0.4f,    // std_acc_y
                0.5f,    // std_acc_z
                0.1f     // std_gyro_x
        };
    }

    /**
     * Create realistic features for RUNNING
     */
    private static float[] createRunningFeatures() {
        return new float[]{
                1.2f,    // mean_acc_x (high movement)
                0.8f,    // mean_acc_y
                9.0f,    // mean_acc_z
                0.15f,   // mean_gyro_x
                0.15f,   // mean_gyro_y
                0.2f,    // mean_gyro_z
                1.0f,    // std_acc_x (high variance)
                0.9f,    // std_acc_y
                1.2f,    // std_acc_z
                0.3f     // std_gyro_x
        };
    }

    private static String mapPredictionToLabel(int classId) {
        switch (classId) {
            case 0: return "WALKING";
            case 1: return "SITTING";
            case 2: return "STANDING";
            case 3: return "RUNNING";
            default: return "UNKNOWN";
        }
    }
}