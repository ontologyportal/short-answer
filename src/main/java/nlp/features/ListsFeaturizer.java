/*
 *  This code is copyright CloudMinds 2017.
 *
 *  Author: Yan Virin jan.virin@gmail.com
 *
 *  This software is released under the GNU Public License <http://www.gnu.org/copyleft/gpl.html>.
 *  Please cite the following article in any publication with references:
 *  Pease A., and Benzmüller C. (2013). Sigma: An Integrated Development Environment for Logical Theories. AI Communications 26, pp79-97.
 */

package nlp.features;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

/**
 * A featurizer for authority lists(gazetteers)
 */
public class ListsFeaturizer extends WordFeaturizer {

    private final static String LIST = "LIST";

    private Map<String, String> index = new HashMap<>();

    /****************************************************************
     * Loads the lists into a map by each list
     */
    public ListsFeaturizer(Path path) throws IOException {

        Files.list(path).forEach(file -> {
            try {
                Files.lines(file).forEach(line -> {
                    index.put(line, file.getFileName().toString());
                });
            }
            catch (IOException e) {
                throw new RuntimeException("Loading failed.");
            }
        });
    }

    /****************************************************************
     * @return Whether to craete bigrams with those features
     */
    @Override
    boolean doBigrams() {
        return false;
    }

    /****************************************************************
     * @return The feature vector with the lists features
     */
    @Override
    SparseFeatureVector featurize(String word) {

        SparseFeatureVector result = new SparseFeatureVector();

        if (index.containsKey(word)) {
            result.add(String.format("%s_%s", LIST, index.get(word)));
        }
        return result;
    }
}
