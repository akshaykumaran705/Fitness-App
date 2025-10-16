import ml.dmlc.xgboost4j.java.Booster;
import ml.dmlc.xgboost4j.java.DMatrix;
import ml.dmlc.xgboost4j.java.XGBoost;
import ml.dmlc.xgboost4j.java.XGBoostError;

import java.util.Arrays;

/**
 * Test the EXACT model file that Spring Boot is using
 */
public class TestSpringBootModel {

    public static void main(String[] args) throws Exception {
        // Test BOTH model files
        String trainedModel = "./activity_model.bst";
        String springModel = "../activityservice/src/main/resources/activity_model.bst";

        System.out.println("=================================================");
        System.out.println("Testing Model Files");
        System.out.println("=================================================\n");

        // Features from your actual Spring Boot logs (SITTING data)
        float[] sittingFeatures = {
                0.0114375f, 0.020015625f, 9.799922f,
                0.001984375f, 0.0020078125f, 0.002f,
                0.002805366f, 0.002556401f, 0.019861571f, 8.131244E-4f
        };

        System.out.println("Testing with ACTUAL features from your logs:");
        System.out.println(Arrays.toString(sittingFeatures));
        System.out.println();

        // Test trained model
        System.out.println("1. Testing TRAINED model: " + trainedModel);
        testModel(trainedModel, sittingFeatures);

        System.out.println();

        // Test Spring Boot model
        System.out.println("2. Testing SPRING BOOT model: " + springModel);
        testModel(springModel, sittingFeatures);

        System.out.println("\n=================================================");
    }

    private static void testModel(String modelPath, float[] features) throws XGBoostError {
        try {
            Booster model = XGBoost.loadModel(modelPath);
            DMatrix dMatrix = new DMatrix(features, 1, features.length);
            float[][] prediction = model.predict(dMatrix);
            float rawPrediction = prediction[0][0];
            int predictedClass = Math.round(rawPrediction);

            String label = mapPredictionToLabel(predictedClass);

            System.out.println("  Raw prediction: " + rawPrediction);
            System.out.println("  Predicted class: " + predictedClass);
            System.out.println("  Predicted label: " + label);

            if (label.equals("SITTING")) {
                System.out.println("  ✓ CORRECT");
            } else {
                System.out.println("  ✗ WRONG - Expected SITTING");
            }
        } catch (Exception e) {
            System.out.println("  ✗ ERROR: " + e.getMessage());
        }
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