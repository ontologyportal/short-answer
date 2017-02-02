package nlp.features;

import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.util.Pair;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public abstract class WordFeaturizer implements Combiner {

    abstract boolean doBigrams();

    abstract SparseFeatureVector featurize(String word);

    public SparseFeatureVector featurize(IndexedWord word) {
        return featurize(word.word());
    }

    public SparseFeatureVector featurize(List<Pair<IndexedWord, String>> extractedWords) {

        SparseFeatureVector result = new SparseFeatureVector();

        // generate the nlp.features
        List<SparseFeatureVector> wordFeatures = new ArrayList<>();
        extractedWords.forEach(w -> wordFeatures.add(featurize(w.first).prefix(w.second)));

        // mergeWith them all into one for unigrams bag of words
        wordFeatures.forEach(result::mergeWith);

        // bigrams
        // sort the words by the index for bigrams nlp.features
        if (doBigrams()) {
            extractedWords.sort(Comparator.comparingInt(w -> w.first.index()));
            IntStream.range(0, wordFeatures.size() - 1).forEach(i ->
                    result.mergeWith(wordFeatures.get(i).cross(wordFeatures.get(i + 1))));
        }

        return result;
    }

    @Override
    public WordFeaturizer combine(WordFeaturizer featurizer) {
        CombiningFeaturizer combiningFeaturizer = new CombiningFeaturizer();
        return combiningFeaturizer.combine(this).combine(featurizer);
    }
}

class CombiningFeaturizer extends WordFeaturizer implements Combiner {

    private List<WordFeaturizer> featurizers = new ArrayList<>();

    @Override
    boolean doBigrams() {
        throw new RuntimeException("Should not be used!");
    }

    @Override
    SparseFeatureVector featurize(String word) {
        throw new RuntimeException("Should not be used!");
    }

    public SparseFeatureVector featurize(List<Pair<IndexedWord, String>> extractedWords) {
        return SparseFeatureVector.merge(featurizers.stream().
                map(r -> r.featurize(extractedWords)).collect(Collectors.toList()));
    }

    @Override
    public WordFeaturizer combine(WordFeaturizer featurizer) {
        featurizers.add(featurizer);
        return this;
    }
}

interface Combiner {
    WordFeaturizer combine(WordFeaturizer other);
}
