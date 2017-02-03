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
import edu.stanford.nlp.process.WordShapeClassifier;

/**
 * A featurizer for lexical features of a word
 */
public class LexicalizedFeaturizer extends WordFeaturizer {

    private final static String LEXICALIZED = "LEX";

    /****************************************************************
     * @return Whether bigrams should be created with those features
     */
    @Override
    boolean doBigrams() {

        return true;
    }

    /****************************************************************
     * @return a feature vector containing just the word itself
     */
    @Override
    public SparseFeatureVector featurize(String word) {

        SparseFeatureVector result = new SparseFeatureVector();
        result.add(String.format("%s_%s", LEXICALIZED, word));
        return result;
    }

    /****************************************************************
     * @return feature vector with shape and lemma features
     *
     * Lemma, Shape
     */
    @Override
    public SparseFeatureVector featurize(IndexedWord word) {

        // add lemma nlp.features
        SparseFeatureVector result = featurize(word.lemma());

        // add shape nlp.features
        SparseFeatureVector shape = featurize(WordShapeClassifier.wordShape(word.word(), WordShapeClassifier.WORDSHAPEDAN2));

        result.mergeWith(shape);

        return result;
    }
}
