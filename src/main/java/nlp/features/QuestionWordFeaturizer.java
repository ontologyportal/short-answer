package nlp.features;


public class QuestionWordFeaturizer extends LexicalizedFeaturizer {

    private final static String QUESTION = "QUESTION";

    @Override
    boolean doBigrams() {
        return false;
    }

    @Override
    public SparseFeatureVector featurize(String word) {
        return super.featurize(String.format("%s_%s", QUESTION, word));
    }
}
