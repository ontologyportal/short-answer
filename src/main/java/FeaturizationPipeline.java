import features.*;

import java.io.IOException;
import java.nio.file.Paths;

public class FeaturizationPipeline {

    private final static String dependencyModel = "english_UD.gz";
    private final static String browClustersModel = "brown-rcv1.clean.tokenized-CoNLL03.txt-c1000-freq1.txt";
    private final static String wordvecModel = "glove.6B.50d.txt";
    private final static String listDir = "lists";
    private final QuestionFociExtractor extractor;
    private final WordFeaturizer pipeline;

    public FeaturizationPipeline(String modelsPath) throws IOException {

        this.extractor = new QuestionFociExtractor(Paths.get(modelsPath, dependencyModel).toString());

        LexicalizedFeaturizer lexicalizedFeaturizer = new LexicalizedFeaturizer();
        BrownClusters clusters = BrownClustersFeaturizer.loadClusters(Paths.get(modelsPath, browClustersModel));
        WordVecFeaturizer wordVecFeaturizer = new WordVecFeaturizer(Paths.get(modelsPath, wordvecModel));
        ListsFeaturizer listsFeaturizer = new ListsFeaturizer(Paths.get(modelsPath, listDir));

        this.pipeline = lexicalizedFeaturizer.combine(wordVecFeaturizer).
                combine(listsFeaturizer).
                combine(new BrownClustersFeaturizer(clusters, 4)).
                combine(new BrownClustersFeaturizer(clusters, 6)).
                combine(new BrownClustersFeaturizer(clusters, 10)).
                combine(new BrownClustersFeaturizer(clusters, 20)).
                combine(new NerFeaturizer());
    }

    public SparseFeatureVector featurize(String sentence) {
        QuestionFociTerms qt = extractor.extractQuestionFociWordsWithType(sentence);
        SparseFeatureVector fromPipeline = pipeline.featurize(qt.termsWithTypes);
        fromPipeline.mergeWith(new QuestionWordFeaturizer().featurize(qt.questionWord));

        // TODO: remove, just for debugging
        if (qt.questionWord.isEmpty()) System.out.println("Empty question word for sentence: " + sentence);
        if (qt.termsWithTypes.isEmpty()) System.out.println("Empty feature set on sentence: " + sentence);
        return fromPipeline;
    }
}
