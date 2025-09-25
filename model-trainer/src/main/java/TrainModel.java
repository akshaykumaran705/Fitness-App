import ml.dmlc.xgboost4j.java.Booster;
import ml.dmlc.xgboost4j.java.DMatrix;
import ml.dmlc.xgboost4j.java.XGBoost;
import ml.dmlc.xgboost4j.java.XGBoostError;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TrainModel {

    public static void main(String[] args) throws IOException, XGBoostError {
        // --- IMPORTANT ---
        // UPDATE THIS PATH to point to your dataset folder.
        // Make sure the CSV file is named 'train.csv' inside this folder.
        String datasetPath = "/Users/akshaykumaran/Documents/project/SpringBoot/model-trainer/dataset/";

        // 1. Load the training data from the single CSV file
        System.out.println("Loading data from train.csv...");
        DMatrix trainMatrix = loadDataFromCsv(datasetPath + "train.csv");
        System.out.println("Data loaded successfully.");

        // 2. Define the parameters for the XGBoost model
        Map<String, Object> params = new HashMap<>();
        params.put("objective", "multi:softmax"); // for multi-class classification
        params.put("num_class", 6);               // There are 6 activities in the dataset
        params.put("eta", 0.1);                   // learning rate
        params.put("max_depth", 4);               // max depth of a tree
        params.put("eval_metric", "merror");      // evaluation metric
        params.put("silent", 1);

        // 3. Train the model
        System.out.println("Training model...");
        int numRounds = 100; // number of training rounds
        Booster model = XGBoost.train(trainMatrix, params, numRounds, new HashMap<>(), null, null);
        System.out.println("Model training complete.");

        // 4. Save the trained model to a file
        String modelPath = "./activity_model.bst";
        model.saveModel(modelPath);
        System.out.println("Model saved successfully to: " + modelPath);
        System.out.println("\nACTION: Copy this 'activity_model.bst' file to your Spring Boot project's 'src/main/resources' directory.");
    }

    /**
     * Helper method to load data from a single CSV file into XGBoost's DMatrix format.
     */
    private static DMatrix loadDataFromCsv(String csvPath) throws IOException, XGBoostError {
        List<Float> labelsList = new ArrayList<>();
        List<Float> featuresList = new ArrayList<>();
        int numRows = 0;

        // This map converts the string labels from the CSV to the numbers our model needs.
        Map<String, Float> activityToLabel = Map.of(
                "WALKING", 0.0f,
                "WALKING_UPSTAIRS", 1.0f,
                "WALKING_DOWNSTAIRS", 2.0f,
                "SITTING", 3.0f,
                "STANDING", 4.0f,
                "LAYING", 5.0f
        );

        try (BufferedReader br = new BufferedReader(new FileReader(csvPath))) {
            String line;
            // Skip the header line
            br.readLine();

            while ((line = br.readLine()) != null) {
                String[] values = line.trim().split(",");
                int numColumns = values.length;

                // The last column is the activity label
                String activity = values[numColumns - 1];
                if (activityToLabel.containsKey(activity)) {
                    labelsList.add(activityToLabel.get(activity));

                    // The first N-1 columns are the features
                    for (int i = 0; i < numColumns - 2; i++) { // Ignore the 'subject' column as well
                        featuresList.add(Float.parseFloat(values[i]));
                    }
                    numRows++;
                }
            }
        }

        // Convert lists to arrays
        float[] labels = new float[labelsList.size()];
        for (int i = 0; i < labelsList.size(); i++) labels[i] = labelsList.get(i);

        float[] features = new float[featuresList.size()];
        for (int i = 0; i < featuresList.size(); i++) features[i] = featuresList.get(i);

        // The number of features per row
        int numFeaturesPerRow = features.length / numRows;

        return new DMatrix(features, numRows, numFeaturesPerRow, Float.NaN);
    }
}

