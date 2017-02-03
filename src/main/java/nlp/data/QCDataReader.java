/*
 *  This code is copyright CloudMinds 2017.
 *
 *  Author: Yan Virin jan.virin@gmail.com
 *
 *  This software is released under the GNU Public License <http://www.gnu.org/copyleft/gpl.html>.
 *  Please cite the following article in any publication with references:
 *  Pease A., and Benzmüller C. (2013). Sigma: An Integrated Development Environment for Logical Theories. AI Communications 26, pp79-97.
 */

package nlp.data;

import edu.stanford.nlp.util.Pair;
import nlp.features.QCFeaturizationPipeline;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Reads the data needed for Question Classification according to UIUC
 * The reader also featurizes the datapoints
 */
public class QCDataReader {

    private Labels labels = new Labels();

    private DataSet train;

    private DataSet test;

    /****************************************************************
     * Creates a new instance of the reader
     * @param gross Whether to parse the input data for "gross" labels classification
     */
    public QCDataReader(Path trainPath, Path testPath, boolean gross, QCFeaturizationPipeline pipeline, String charsetName) throws IOException {

        Charset charset = Charset.forName(charsetName);
        this.train = read(trainPath, gross, pipeline, charset);
        this.test = read(testPath, gross, pipeline, charset);
        this.train.labels = labels;
        this.test.labels = labels;
    }

    /****************************************************************
     * @return A dataset that contains all the data
     */
    private DataSet read(Path dataPath, boolean gross, QCFeaturizationPipeline pipeline, Charset charset) throws IOException {

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
                throw new RuntimeException("Failed to read from " + file);
            }
        });

        return dataSet;
    }

    /****************************************************************
     * @return The training set
     */
    public DataSet getTrain() { return train; }

    /****************************************************************
     * @return The test set. Note: This is not to be used a validation set,
     *         but rather as an official test set for a task. For generating dev
     *         and validation sets see @DataSet.split(double) method.
     */
    public DataSet getTest() { return test; }

    /****************************************************************
     * @return The labels read during loading of the dataset
     */
    public Labels getLabels() { return labels; }
}
