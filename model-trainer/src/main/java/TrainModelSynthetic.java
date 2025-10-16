import ml.dmlc.xgboost4j.java.Booster;
import ml.dmlc.xgboost4j.java.DMatrix;
import ml.dmlc.xgboost4j.java.XGBoost;
import ml.dmlc.xgboost4j.java.XGBoostError;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

/**
 * Train model on synthetic data with raw sensor values
 */
public class TrainModelSynthetic {

    public static void main(String[] args) throws IOException, XGBoostError {
        String csvFile = "/Users/akshaykumaran/Documents/project/SpringBoot/model-trainer/dataset/synthetic_train.csv";

        System.out.println("=================================================");
        System.out.println("Training on Synthetic Raw Sensor Data");
        System.out.println("=================================================\n");

        DMatrix trainMatrix = loadSyntheticData(csvFile);

        // Check if data loaded successfully
        if (trainMatrix.rowNum() == 0) {
            System.err.println("❌ ERROR: No data loaded from " + csvFile);
            System.err.println("Please check CSV formatting (should be 10 features + 1 activity column).");
            return;
        }

        System.out.println("✅ Data loaded successfully");
        System.out.println("Samples: " + trainMatrix.rowNum());
        trainAndSaveModel(trainMatrix);
    }

    private static DMatrix loadSyntheticData(String csvFile) throws IOException, XGBoostError {
        Map<String, Float> activityMap = new HashMap<>();
        activityMap.put("WALKING", 0.0f);
        activityMap.put("SITTING", 1.0f);
        activityMap.put("STANDING", 2.0f);
        activityMap.put("RUNNING", 3.0f);

        List<float[]> allFeatures = new ArrayList<>();
        List<Float> allLabels = new ArrayList<>();
        Map<String, Integer> activityCounts = new HashMap<>();

        try (BufferedReader br = new BufferedReader(new FileReader(csvFile))) {
            br.readLine(); // Skip header

            String line;
            while ((line = br.readLine()) != null) {
                String[] values = line.split(",");
                if (values.length != 11) continue; // 10 features + 1 label

                String activity = values[10].trim();
                if (!activityMap.containsKey(activity)) continue;

                float[] features = new float[10];
                try {
                    for (int i = 0; i < 10; i++) {
                        features[i] = Float.parseFloat(values[i]);
                    }
                } catch (NumberFormatException e) {
                    continue; // Skip malformed rows
                }

                allFeatures.add(features);
                allLabels.add(activityMap.get(activity));
                activityCounts.merge(activity, 1, Integer::sum);
            }
        }

        System.out.println("Loaded " + allFeatures.size() + " samples");
        System.out.println("\nActivity distribution:");
        activityCounts.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(e -> System.out.println("  " + e.getKey() + " (class " +
                        activityMap.get(e.getKey()).intValue() + "): " + e.getValue()));

        // Convert to DMatrix
        float[] labels = new float[allLabels.size()];
        for (int i = 0; i < allLabels.size(); i++) {
            labels[i] = allLabels.get(i);
        }

        float[] features = new float[allFeatures.size() * 10];
        for (int i = 0; i < allFeatures.size(); i++) {
            System.arraycopy(allFeatures.get(i), 0, features, i * 10, 10);
        }

        DMatrix dmat = new DMatrix(features, allFeatures.size(), 10, Float.NaN);
        dmat.setLabel(labels);
        return dmat;
    }

    private static void trainAndSaveModel(DMatrix trainMatrix) throws XGBoostError {
        Map<String, Object> params = new HashMap<>();
        params.put("objective", "multi:softmax");
        params.put("num_class", 4);
        params.put("eta", 0.3);
        params.put("max_depth", 6);
        params.put("subsample", 0.8);
        params.put("colsample_bytree", 0.8);
        params.put("eval_metric", "merror");
        params.put("seed", 42);
        params.put("tree_method", "hist"); // ✅ CPU mode
        params.put("device", "cpu");       // ✅ Prevent CUDA call

        System.out.println("\n=================================================");
        System.out.println("Training Model");
        System.out.println("=================================================\n");

        Map<String, DMatrix> watches = new HashMap<>();
        watches.put("train", trainMatrix);

        // Train model safely
        Booster model = XGBoost.train(trainMatrix, params, 100, watches, null, null);
        System.out.println("\n✅ Training complete");

        // Evaluate performance
        evaluateModel(model, trainMatrix);

        // Save trained model
        String modelPath = "./Users/akshaykumaran/Documents/project/SpringBoot/activityservice/src/main/resources";
        model.saveModel(modelPath);

        System.out.println("\n=================================================");
        System.out.println("✅ Model saved to: " + modelPath);
        System.out.println("=================================================");
        System.out.println("\nNext steps:");
        System.out.println("1. Test: java -cp \".:lib/*\" DebugPrediction");
        System.out.println("2. Deploy: cp activity_model.bst ../activityservice/src/main/resources/");
        System.out.println("=================================================\n");
    }

    private static void evaluateModel(Booster model, DMatrix trainMatrix) throws XGBoostError {
        float[][] predictions = model.predict(trainMatrix);
        float[] labels = trainMatrix.getLabel();

        int total = predictions.length;
        int correct = 0;
        int[] classCounts = new int[4];
        int[] classCorrect = new int[4];

        for (int i = 0; i < total; i++) {
            int predicted = Math.round(predictions[i][0]);
            int actual = Math.round(labels[i]);

            classCounts[actual]++;
            if (predicted == actual) {
                correct++;
                classCorrect[actual]++;
            }
        }

        System.out.println("\n=== Training Results ===");
        System.out.println("Overall Accuracy: " + String.format("%.2f%%", 100.0 * correct / total));
        System.out.println("\nPer-class Performance:");

        String[] activityNames = {"WALKING", "SITTING", "STANDING", "RUNNING"};
        for (int i = 0; i < 4; i++) {
            if (classCounts[i] > 0) {
                double accuracy = 100.0 * classCorrect[i] / classCounts[i];
                System.out.println(String.format("  %s: %.2f%% (%d/%d)",
                        activityNames[i], accuracy, classCorrect[i], classCounts[i]));
            }
        }
    }
}
