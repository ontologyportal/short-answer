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

import nlp.qa.QuestionFociExtractor;
import nlp.qa.QuestionFociTerms;

import java.io.IOException;
import java.nio.file.Paths;

/**
 * The featurization pipeline for question classification
 */
public class QCFeaturizationPipeline {

    private final static String browClustersModel = "brown-rcv1.clean.tokenized-CoNLL03.txt-c1000-freq1.txt";
    private final static String wordvecModel = "glove.6B.50d.txt";
    private final static String listDir = "lists";

    private final QuestionFociExtractor extractor;
    private final WordFeaturizer pipeline;

    /****************************************************************
     * Creates the featurization pipeline which loads all the models
     */
    public QCFeaturizationPipeline(String modelsPath) throws IOException {

        this.extractor = new QuestionFociExtractor();

        LexicalizedFeaturizer lexicalizedFeaturizer = new LexicalizedFeaturizer();
        BrownClusters clusters = BrownClustersFeaturizer.loadClusters(Paths.get(modelsPath, browClustersModel));
        WordVecFeaturizer wordVecFeaturizer = new WordVecFeaturizer(Paths.get(modelsPath, wordvecModel));
        ListsFeaturizer listsFeaturizer = new ListsFeaturizer(Paths.get(modelsPath, listDir));

        this.pipeline = lexicalizedFeaturizer.
                combine(wordVecFeaturizer).
                combine(listsFeaturizer).
                combine(new BrownClustersFeaturizer(clusters, 4)).
                combine(new BrownClustersFeaturizer(clusters, 6)).
                combine(new BrownClustersFeaturizer(clusters, 10)).
                combine(new BrownClustersFeaturizer(clusters, 20)).
                combine(new NerFeaturizer());
    }

    /****************************************************************
     * @return a feature vector representing the sentence
     */
    public SparseFeatureVector featurize(String sentence) {

        // extract the important information from the sentence
        QuestionFociTerms qt = extractor.extractQuestionFociWordsWithType(sentence);

        // run through the general pipeline
        SparseFeatureVector fromPipeline = pipeline.featurize(qt.termsWithTypes);

        // merge with the special question word feature
        fromPipeline.mergeWith(new QuestionWordFeaturizer().featurize(qt.questionWord));
        fromPipeline.mergeWith(new QuestionWordFeaturizer().featurize(qt.questionType));

        return fromPipeline;
    }
}
