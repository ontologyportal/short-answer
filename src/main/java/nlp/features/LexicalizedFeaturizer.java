package nlp.features;

import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.process.WordShapeClassifier;

public class LexicalizedFeaturizer extends WordFeaturizer {

    private final static String LEXICALIZED = "LEX";

    @Override
    boolean doBigrams() {
        return true;
    }

    @Override
    public SparseFeatureVector featurize(String word) {
        SparseFeatureVector result = new SparseFeatureVector();
        result.add(String.format("%s_%s", LEXICALIZED, word));
        return result;
    }

    /**
     * Lemma, Shape
     * @param word The original word
     * @return Lexicalized nlp.features for the word.
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
