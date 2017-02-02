package nlp.data;

import edu.stanford.nlp.util.Pair;
import nlp.features.ClassificationFeaturizationPipeline;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Reads the data and featurizes it
 */
public class DataReader {

    private Labels labels = new Labels();
    private DataSet train;
    private DataSet test;

    public DataReader(Path trainPath, Path testPath, boolean gross, ClassificationFeaturizationPipeline pipeline, String charsetName) throws IOException {
        Charset charset = Charset.forName(charsetName);
        this.train = read(trainPath, gross, pipeline, charset);
        this.test = read(testPath, gross, pipeline, charset);
        this.train.labels = labels;
        this.test.labels = labels;
    }

    private DataSet read(Path dataPath, boolean gross, ClassificationFeaturizationPipeline pipeline, Charset charset) throws IOException {

        DataSet dataSet = new DataSet();

        Files.list(dataPath).forEach(file -> {
            try {
                Files.lines(file, charset).forEach(line -> {
                    String[] vals = line.split("\\s+");
                    String label = new String(vals[0]);
                    String question = line.substring(label.length() + 1);
                    if (gross) {
                        label = label.split(":")[0];
                    }

                    // index the label
                    int labelIndex = labels.index(label);
                    dataSet.rawDataPoints.add(new Pair<>(label, question));
                    dataSet.dataPoints.add(new Pair<>(labelIndex, pipeline.featurize(question)));
                });
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        });

        return dataSet;
    }

    public DataSet getTrain() { return train; }
    public DataSet getTest() { return test; }
    public Labels getLabels() { return labels; }
}
