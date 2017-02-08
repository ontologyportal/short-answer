/*
 *  This code is copyright CloudMinds 2017.
 *
 *  Author: Yan Virin jan.virin@gmail.com
 *
 *  This software is released under the GNU Public License <http://www.gnu.org/copyleft/gpl.html>.
 *  Please cite the following article in any publication with references:
 *  Pease A., and Benzmüller C. (2013). Sigma: An Integrated Development Environment for Logical Theories. AI Communications 26, pp79-97.
 */

package nlp.qa;

import edu.emory.clir.clearnlp.dependency.DEPNode;
import edu.emory.clir.clearnlp.dependency.DEPTree;
import edu.emory.clir.clearnlp.srl.SRLTree;
import edu.emory.clir.clearnlp.util.arc.SRLArc;
import edu.stanford.nlp.util.Pair;
import edu.stanford.nlp.util.Sets;
import nlp.features.AnswerExtractionFeaturizer;
import nlp.features.QCFeaturizationPipeline;
import nlp.features.SparseFeatureVector;
import nlp.learning.Scorer;
import nlp.semantics.SemanticParser;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * This is a manually set scorer of the features for extracting the answer
 */
class ManuallySetExtractingScorer implements Scorer {

    private SparseFeatureVector weightsVector;

    public ManuallySetExtractingScorer() {

        // define the weights here
        // under current manual approach, all those features represent some negative
        // effect which should be penalized
        weightsVector = new SparseFeatureVector();
        weightsVector.add(AnswerExtractionFeaturizer.featureName(AnswerExtractionFeaturizer.ARGM), -1.0);
        weightsVector.add(AnswerExtractionFeaturizer.featureName(AnswerExtractionFeaturizer.PREP), -1.0);
        weightsVector.add(AnswerExtractionFeaturizer.featureName(AnswerExtractionFeaturizer.WORD), -1.0);
        weightsVector.add(AnswerExtractionFeaturizer.featureName(AnswerExtractionFeaturizer.LEMMA), -1.0);
        weightsVector.add(AnswerExtractionFeaturizer.featureName(AnswerExtractionFeaturizer.LOWERCASE), -1.0);
        weightsVector.add(AnswerExtractionFeaturizer.featureName(AnswerExtractionFeaturizer.WH), -1.0);
        weightsVector.add(AnswerExtractionFeaturizer.featureName(AnswerExtractionFeaturizer.ADVERB), -1.0);
        weightsVector.add(AnswerExtractionFeaturizer.featureName(AnswerExtractionFeaturizer.SEMANTIC_NONMATCH), -1.0);
    }

    /****************************************************************
     * @return scoring the datapoint with the manual weights vector
     */
    @Override
    public List<Pair<String, Double>> score(SparseFeatureVector dataPoint) {

        return Collections.singletonList(new Pair<>("BEST", dataPoint.dot(weightsVector)));
    }
}

/**
 *
 */
public class ShortAnswerExtractor {

    private final SemanticParser semanticParser;

    private final Scorer classifier;

    private final QCFeaturizationPipeline classificationFeaturizer;

    private final Scorer extractingScorer;

    private final AnswerExtractionFeaturizer extractingFeaturizer;

    /****************************************************************
     * @return instance of the extractor initialized with all the models
     */
    public ShortAnswerExtractor(SemanticParser semanticParser, Scorer classifier,
                                QCFeaturizationPipeline classificationFeaturizer) {

        this.semanticParser = semanticParser;
        this.classifier = classifier;
        this.classificationFeaturizer = classificationFeaturizer;
        this.extractingScorer = new ManuallySetExtractingScorer();
        this.extractingFeaturizer = new AnswerExtractionFeaturizer();
    }

    /****************************************************************
     * @return a single word extracted from the @param answer,
     *         as a short answer to the @param question
     */
    public String extract(String question, String answer) {

        List<Pair<String, Double>> predicted = classifier.score(classificationFeaturizer.featurize(answer));

        // based on the class and semantic roles for both sentence and question - extract the extract
        DEPTree questionParsed = semanticParser.parse(question);
        DEPTree answerParsed = semanticParser.parse(answer);

        // TODO: run through all the inner extractors; right now using only the generic extraction technique
        String questionCategory = classifier.score(classificationFeaturizer.featurize(question)).get(0).first;


        


        DEPNode result = genericExtract(questionParsed, answerParsed, questionCategory);
        if (result != null)
            return result.getWordForm();

        return null;
    }

    /****************************************************************
     * makes a generic attempt to extract a short answer given semantic roles labeled graphs
     * and questions category
     */
    private DEPNode genericExtract(DEPTree questionParsed, DEPTree answerParsed, String questionCategory) {

        DEPNode questionNode = findQuestionWord(questionParsed);
        DEPNode verbNode = findVerb(questionNode, questionParsed);
        DEPNode matchedNode = matchSemantics(verbNode, answerParsed);

        // featurize all the candidates
        List<Pair<SRLArc, SparseFeatureVector>> featurizedCandidates = answerParsed.getSRLTree(matchedNode).getArgumentArcList().stream().map(a ->
                new Pair<>(a, extractingFeaturizer.featurize(a, getSemanticArcs(verbNode, questionParsed), questionCategory))).
                collect(Collectors.toList());

        // score all the candidates with a scorer
        List<Pair<SRLArc, Double>> best = featurizedCandidates.stream().map(c -> new Pair<>(c.first, extractingScorer.score(c.second).get(0).second)).
                sorted(Comparator.comparingDouble(d -> -d.second)).collect(Collectors.toList());

        if (best.size() == 0 || (best.size() > 1 && best.get(0).second == best.get(1).second))
            return null;

        return best.get(0).first.getNode();
    }

    /****************************************************************
     * @return
     */
    private List<SRLArc> getSemanticArcs(DEPNode node, DEPTree tree) {

        SRLTree srlTree = tree.getSRLTree(node);
        if (srlTree == null)
            return new ArrayList<>();
        return srlTree.getArgumentArcList();
    }

    /****************************************************************
     * @return
     */
    private Pair<DEPNode, Integer> findNode(DEPNode target, DEPNode source, int depth, Function<DEPNode, Boolean> predicate, DEPTree tree) {

        if (predicate.apply(target)) return new Pair<>(source != null ? source : target, depth);
        for (SRLArc arc : getSemanticArcs(target, tree)) {
            Pair<DEPNode, Integer> verb = findNode(arc.getNode(), source, depth + 1, predicate, tree);
            if (verb != null) return verb;
        }
        return null;
    }

    /****************************************************************
     * @return
     */
    private DEPNode findVerb(DEPNode questionNode, DEPTree tree) {

        Pair<DEPNode, Integer> verb1 = findNode(questionNode, null, 0, n -> n.getPOSTag().startsWith("V"), tree);
        List<Pair<DEPNode, Integer>> res = new ArrayList<>();
        for (DEPNode n : tree) {
            if (n.getPOSTag().startsWith("V")) {
                Pair<DEPNode, Integer> node = findNode(n, n, 0, x -> x.equals(questionNode), tree);
                if (node != null) res.add(node);
            }
        }
        if (verb1 != null) res.add(verb1);
        return res.stream().min(Comparator.comparingInt(x -> x.second)).orElse(new Pair<>(null, 0)).first;
    }

    /****************************************************************
     * @return
     */
    private DEPNode findQuestionWord(DEPTree questionTree) {

        return Arrays.stream(questionTree.toNodeArray()).filter(n ->
                QuestionFociExtractor.questionWords.contains(n.getWordForm().toLowerCase())).findFirst().get();
    }

    /****************************************************************
     * @return
     */
    private double score(List<SRLArc> l1, List<SRLArc> l2) {

        HashSet<String> s1 = new HashSet<>(l1.stream().map(a -> a.getLabel()).collect(Collectors.toList()));
        HashSet<String> s2 = new HashSet<>(l2.stream().map(a -> a.getLabel()).collect(Collectors.toList()));

        return Sets.intersection(s1, s2).size() / (double) Sets.union(s1, s2).size();
    }

    /****************************************************************
     * @return
     */
    private DEPNode matchSemantics(DEPNode target, DEPTree tree) {

        List<SRLArc> targetArcs = getSemanticArcs(target, tree);
        List<Pair<DEPNode, Double>> scoredNodes = new ArrayList<>();
        for (DEPNode n : tree) {
            scoredNodes.add(new Pair<>(n, score(targetArcs, getSemanticArcs(n, tree))));
        }
        List<Pair<DEPNode, Double>> sorted = scoredNodes.stream().sorted(Comparator.comparingDouble(n -> -n.second)).collect(Collectors.toList());
        if (sorted.isEmpty() || (sorted.size() > 1 && sorted.get(0).second == sorted.get(1).second))
            return null;
        return sorted.get(0).first;
    }
}
