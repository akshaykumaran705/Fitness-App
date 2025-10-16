import ml.dmlc.xgboost4j.java.Booster;
import ml.dmlc.xgboost4j.java.DMatrix;
import ml.dmlc.xgboost4j.java.XGBoost;
import ml.dmlc.xgboost4j.java.XGBoostError;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

/**
 * Train model using ONLY 10 features that match runtime feature extraction.
 *
 * From the UCI HAR 561-feature dataset, we extract:
 * - tBodyAcc-mean()-X, Y, Z (columns 0, 1, 2)
 * - tBodyGyro-mean()-X, Y, Z (columns 120, 121, 122)
 * - tBodyAcc-std()-X, Y, Z (columns 3, 4, 5)
 * - tBodyGyro-std()-X (column 123)
 */
public class TrainModel {

    public static void main(String[] args) throws IOException, XGBoostError {
        String datasetPath = "/Users/akshaykumaran/Documents/project/SpringBoot/model-trainer/dataset/";
        String csvFile = datasetPath + "synthetic_train.csv";

        System.out.println("=================================================");
        System.out.println("Activity Recognition Model Trainer");
        System.out.println("Training with 10 features matching runtime extraction");
        System.out.println("=================================================\n");

        int[] featureIndices = {
                0,   // tBodyAcc-mean()-X
                1,   // tBodyAcc-mean()-Y
                2,   // tBodyAcc-mean()-Z
                120, // tBodyGyro-mean()-X
                121, // tBodyGyro-mean()-Y
                122, // tBodyGyro-mean()-Z
                3,   // tBodyAcc-std()-X
                4,   // tBodyAcc-std()-Y
                5,   // tBodyAcc-std()-Z
                123  // tBodyGyro-std()-X
        };

        DMatrix trainMatrix = loadDataWithSelectedFeatures(csvFile, featureIndices);
        System.out.println("Data loaded successfully.\n");

        trainAndSaveModel(trainMatrix);
    }

    private static DMatrix loadDataWithSelectedFeatures(String csvFile, int[] featureIndices)
            throws IOException, XGBoostError {

        Map<String, Float> activityMap = new HashMap<>();
        activityMap.put("WALKING", 0.0f);
        activityMap.put("WALKING_UPSTAIRS", 0.0f);
        activityMap.put("WALKING_DOWNSTAIRS", 0.0f);
        activityMap.put("SITTING", 1.0f);
        activityMap.put("LAYING", 1.0f);
        activityMap.put("STANDING", 2.0f);
        activityMap.put("RUNNING", 3.0f);

        List<float[]> allFeatures = new ArrayList<>();
        List<Float> allLabels = new ArrayList<>();
        Map<String, Integer> activityCounts = new HashMap<>();

        try (BufferedReader br = new BufferedReader(new FileReader(csvFile))) {
            String header = br.readLine(); // skip header
            String line;
            int lineCount = 0;
            int skippedCount = 0;

            while ((line = br.readLine()) != null) {
                String[] values = line.split(",");

                if (values.length < 563) {
                    skippedCount++;
                    continue;
                }

                String activity = values[values.length - 1].trim().toUpperCase();
                if (!activityMap.containsKey(activity)) {
                    skippedCount++;
                    continue;
                }

                try {
                    float[] features = new float[10];
                    for (int i = 0; i < featureIndices.length; i++) {
                        features[i] = Float.parseFloat(values[featureIndices[i]]);
                    }
                    allFeatures.add(features);
                    allLabels.add(activityMap.get(activity));
                    activityCounts.merge(activity, 1, Integer::sum);
                    lineCount++;

                } catch (NumberFormatException e) {
                    skippedCount++;
                }
            }

            System.out.println("Loaded " + lineCount + " samples");
            System.out.println("Skipped " + skippedCount + " samples");
            System.out.println("\nActivity distribution:");

            Map<String, String> labelNames = new HashMap<>();
            labelNames.put("WALKING", "WALKING (class 0)");
            labelNames.put("WALKING_UPSTAIRS", "WALKING_UPSTAIRS → WALKING (class 0)");
            labelNames.put("WALKING_DOWNSTAIRS", "WALKING_DOWNSTAIRS → WALKING (class 0)");
            labelNames.put("SITTING", "SITTING (class 1)");
            labelNames.put("LAYING", "LAYING → SITTING (class 1)");
            labelNames.put("STANDING", "STANDING (class 2)");
            labelNames.put("RUNNING", "RUNNING (class 3)");

            activityCounts.entrySet().stream()
                    .sorted(Map.Entry.comparingByKey())
                    .forEach(e -> System.out.println("  " + labelNames.get(e.getKey()) + ": " + e.getValue()));
        }

        float[] labels = new float[allLabels.size()];
        for (int i = 0; i < allLabels.size(); i++) {
            labels[i] = allLabels.get(i);
        }

        float[] features = new float[allFeatures.size() * 10];
        for (int i = 0; i < allFeatures.size(); i++) {
            System.arraycopy(allFeatures.get(i), 0, features, i * 10, 10);
        }

        System.out.println("\nTotal samples: " + allFeatures.size());
        System.out.println("Features per sample: 10");

        DMatrix dmat = new DMatrix(features, allFeatures.size(), 10, Float.NaN);
        dmat.setLabel(labels); // ✅ attach labels properly
        return dmat;
    }

    private static void trainAndSaveModel(DMatrix trainMatrix) throws XGBoostError {
        // Compute number of unique classes dynamically
        float[] trainLabels = trainMatrix.getLabel();
        Set<Integer> uniqueClasses = new HashSet<>();
        for (float v : trainLabels) uniqueClasses.add(Math.round(v));
        int numClass = uniqueClasses.size();

        Map<String, Object> params = new HashMap<>();
        params.put("objective", "multi:softprob");  // ✅ use softprob for mlogloss
        params.put("num_class", numClass);
        params.put("eta", 0.3);
        params.put("max_depth", 6);
        params.put("subsample", 0.8);
        params.put("colsample_bytree", 0.8);
        params.put("eval_metric", "mlogloss");
        params.put("seed", 42);

        System.out.println("\n=================================================");
        System.out.println("Training XGBoost Model");
        System.out.println("=================================================\n");

        Map<String, DMatrix> watches = new HashMap<>();
        watches.put("train", trainMatrix);

        Booster model = XGBoost.train(trainMatrix, params, 100, watches, null, null);
        System.out.println("\nModel training complete.");

        evaluateModel(model, trainMatrix);

        String modelPath = "./activity_model.bst";
        model.saveModel(modelPath);

        System.out.println("\n=================================================");
        System.out.println("✓ Model saved to: " + modelPath);
        System.out.println("=================================================");
        System.out.println("\nNEXT STEPS:");
        System.out.println("1. Test the model:");
        System.out.println("   java -cp \".:lib/*\" DebugPrediction");
        System.out.println("\n2. If tests pass, deploy:");
        System.out.println("   cp activity_model.bst ../activityservice/src/main/resources/");
        System.out.println("\n3. Restart Spring Boot and test with Postman");
        System.out.println("=================================================\n");
    }

    private static void evaluateModel(Booster model, DMatrix trainMatrix) throws XGBoostError {
        float[][] predictions = model.predict(trainMatrix);
        float[] labels = trainMatrix.getLabel();

        int total = predictions.length;
        if (total == 0) {
            System.out.println("No predictions returned — check data.");
            return;
        }

        boolean isProbabilities = predictions[0].length > 1;
        int numClass = isProbabilities ? predictions[0].length : 4;

        int correct = 0;
        int[] classCounts = new int[numClass];
        int[] classCorrect = new int[numClass];

        for (int i = 0; i < total; i++) {
            int predicted;
            if (isProbabilities) {
                float[] probs = predictions[i];
                int argmax = 0;
                float best = probs[0];
                for (int j = 1; j < probs.length; j++) {
                    if (probs[j] > best) {
                        best = probs[j];
                        argmax = j;
                    }
                }
                predicted = argmax;
            } else {
                predicted = Math.round(predictions[i][0]);
            }

            int actual = Math.round(labels[i]);
            if (actual >= 0 && actual < numClass) {
                classCounts[actual]++;
                if (predicted == actual) {
                    correct++;
                    classCorrect[actual]++;
                }
            }
        }

        System.out.println("\n=== Training Results ===");
        System.out.println("Overall Accuracy: " + String.format("%.2f%%", 100.0 * correct / total));
        System.out.println("\nPer-class Performance:");

        String[] activityNames = {"WALKING", "SITTING", "STANDING", "RUNNING"};
        for (int i = 0; i < numClass; i++) {
            if (classCounts[i] > 0) {
                double accuracy = 100.0 * classCorrect[i] / classCounts[i];
                String name = (i < activityNames.length) ? activityNames[i] : ("class " + i);
                System.out.println(String.format("  %s (class %d): %.2f%% correct (%d/%d samples)",
                        name, i, accuracy, classCorrect[i], classCounts[i]));
            } else {
                System.out.println(String.format("  class %d: No samples in training data", i));
            }
        }

        if (classCounts.length > 1 && classCounts[1] > 0 && classCorrect[1] == 0) {
            System.out.println("\n⚠️  WARNING: Model cannot predict SITTING correctly!");
            System.out.println("   This explains why everything is detected as WALKING.");
        }
    }
}
