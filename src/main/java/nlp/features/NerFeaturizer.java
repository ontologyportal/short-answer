package nlp.features;

import edu.stanford.nlp.ling.IndexedWord;

public class NerFeaturizer extends WordFeaturizer {

    @Override
    boolean doBigrams() {
        return false;
    }

    @Override
    SparseFeatureVector featurize(String word) {
        throw new RuntimeException("Should not be used!");
    }

    @Override
    public SparseFeatureVector featurize(IndexedWord word) {

        SparseFeatureVector result = new SparseFeatureVector();

        if (word.ner() != null) {
            result.add(String.format("%s_%s_%s", "FOCUS", "NER", word.ner()));
        }

        return result;
    }
}
