import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Random;

/**
 * Generate synthetic training data with realistic raw sensor values
 * This matches the scale of real accelerometer/gyroscope data
 */
public class GenerateSyntheticTrainingData {

    private static final int SAMPLES_PER_ACTIVITY = 2000;
    private static final Random random = new Random(42);

    public static void main(String[] args) throws IOException {
        String outputFile = "/Users/akshaykumaran/Documents/project/SpringBoot/model-trainer/dataset/synthetic_train.csv";

        System.out.println("=================================================");
        System.out.println("Generating Synthetic Training Data");
        System.out.println("=================================================\n");

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile))) {
            // Write header (10 features + activity)
            writer.write("mean_acc_x,mean_acc_y,mean_acc_z,mean_gyro_x,mean_gyro_y,mean_gyro_z,");
            writer.write("std_acc_x,std_acc_y,std_acc_z,std_gyro_x,Activity\n");

            // Generate data for each activity
            generateActivityData(writer, "SITTING", SAMPLES_PER_ACTIVITY);
            generateActivityData(writer, "STANDING", SAMPLES_PER_ACTIVITY);
            generateActivityData(writer, "WALKING", SAMPLES_PER_ACTIVITY);
            generateActivityData(writer, "RUNNING", SAMPLES_PER_ACTIVITY);
        }

        System.out.println("\n=================================================");
        System.out.println("✓ Synthetic data generated: " + outputFile);
        System.out.println("Total samples: " + (SAMPLES_PER_ACTIVITY * 4));
        System.out.println("=================================================\n");
        System.out.println("Next step: Train the model");
        System.out.println("  java -cp \".:lib/*\" TrainModelSynthetic");
    }

    private static void generateActivityData(BufferedWriter writer, String activity, int count)
            throws IOException {

        System.out.println("Generating " + count + " samples for " + activity + "...");

        for (int i = 0; i < count; i++) {
            float[] features = generateFeatures(activity);

            // Write as CSV
            for (int j = 0; j < features.length; j++) {
                writer.write(String.format("%.6f", features[j]));
                if (j < features.length - 1) writer.write(",");
            }
            writer.write("," + activity + "\n");
        }
    }

    private static float[] generateFeatures(String activity) {
        float[] features = new float[10];

        switch (activity) {
            case "SITTING":
                // Very stable, minimal movement
                features[0] = gaussian(0.01f, 0.02f);   // mean_acc_x
                features[1] = gaussian(0.02f, 0.02f);   // mean_acc_y
                features[2] = gaussian(9.8f, 0.05f);    // mean_acc_z (gravity)
                features[3] = gaussian(0.001f, 0.005f); // mean_gyro_x
                features[4] = gaussian(0.002f, 0.005f); // mean_gyro_y
                features[5] = gaussian(0.001f, 0.005f); // mean_gyro_z
                features[6] = gaussian(0.02f, 0.01f);   // std_acc_x
                features[7] = gaussian(0.02f, 0.01f);   // std_acc_y
                features[8] = gaussian(0.05f, 0.02f);   // std_acc_z
                features[9] = gaussian(0.005f, 0.003f); // std_gyro_x
                break;

            case "STANDING":
                // Slight swaying, more variance than sitting
                features[0] = gaussian(0.05f, 0.05f);   // mean_acc_x
                features[1] = gaussian(0.05f, 0.05f);   // mean_acc_y
                features[2] = gaussian(9.7f, 0.1f);     // mean_acc_z
                features[3] = gaussian(0.01f, 0.01f);   // mean_gyro_x
                features[4] = gaussian(0.01f, 0.01f);   // mean_gyro_y
                features[5] = gaussian(0.01f, 0.01f);   // mean_gyro_z
                features[6] = gaussian(0.08f, 0.03f);   // std_acc_x
                features[7] = gaussian(0.08f, 0.03f);   // std_acc_y
                features[8] = gaussian(0.1f, 0.03f);    // std_acc_z
                features[9] = gaussian(0.02f, 0.01f);   // std_gyro_x
                break;

            case "WALKING":
                // Moderate rhythmic movement
                features[0] = gaussian(0.5f, 0.2f);     // mean_acc_x
                features[1] = gaussian(0.3f, 0.2f);     // mean_acc_y
                features[2] = gaussian(9.5f, 0.3f);     // mean_acc_z
                features[3] = gaussian(0.05f, 0.03f);   // mean_gyro_x
                features[4] = gaussian(0.05f, 0.03f);   // mean_gyro_y
                features[5] = gaussian(0.08f, 0.03f);   // mean_gyro_z
                features[6] = gaussian(0.4f, 0.1f);     // std_acc_x
                features[7] = gaussian(0.4f, 0.1f);     // std_acc_y
                features[8] = gaussian(0.5f, 0.15f);    // std_acc_z
                features[9] = gaussian(0.1f, 0.03f);    // std_gyro_x
                break;

            case "RUNNING":
                // High intensity movement
                features[0] = gaussian(1.2f, 0.4f);     // mean_acc_x
                features[1] = gaussian(0.8f, 0.4f);     // mean_acc_y
                features[2] = gaussian(9.0f, 0.6f);     // mean_acc_z
                features[3] = gaussian(0.15f, 0.05f);   // mean_gyro_x
                features[4] = gaussian(0.15f, 0.05f);   // mean_gyro_y
                features[5] = gaussian(0.2f, 0.05f);    // mean_gyro_z
                features[6] = gaussian(1.0f, 0.3f);     // std_acc_x
                features[7] = gaussian(0.9f, 0.3f);     // std_acc_y
                features[8] = gaussian(1.2f, 0.3f);     // std_acc_z
                features[9] = gaussian(0.3f, 0.1f);     // std_gyro_x
                break;
        }

        return features;
    }

    private static float gaussian(float mean, float stdDev) {
        return (float) (mean + random.nextGaussian() * stdDev);
    }
}