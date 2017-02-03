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


/**
 * Featurizes the question word
 */
public class QuestionWordFeaturizer extends LexicalizedFeaturizer {

    private final static String QUESTION = "QUESTION";

    /****************************************************************
     * @return Whether to create bigrams
     */
    @Override
    boolean doBigrams() {

        return false;
    }

    /****************************************************************
     * @return A feature vector based on the question word
     */
    @Override
    public SparseFeatureVector featurize(String word) {

        return super.featurize(String.format("%s_%s", QUESTION, word));
    }
}
