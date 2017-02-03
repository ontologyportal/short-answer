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

import edu.stanford.nlp.ling.IndexedWord;

/**
 * A featurizer that adds a feature if a Named Entity is present
 */
public class NerFeaturizer extends WordFeaturizer {

    /****************************************************************
     * @return Whether to create bigrams from the features
     */
    @Override
    boolean doBigrams() {

        return false;
    }

    /****************************************************************
     * @return Should not be used with this featurizer
     */
    @Override
    SparseFeatureVector featurize(String word) {

        throw new RuntimeException("Should not be used!");
    }

    /****************************************************************
     * @return A feature vector based on whether the word participates in an entity
     */
    @Override
    public SparseFeatureVector featurize(IndexedWord word) {

        SparseFeatureVector result = new SparseFeatureVector();

        if (word.ner() != null) {
            result.add(String.format("%s_%s_%s", "FOCUS", "NER", word.ner()));
        }

        return result;
    }
}
