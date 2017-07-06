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

import com.articulate.sigma.WordNetUtilities;
import edu.emory.clir.clearnlp.dependency.DEPNode;
import edu.emory.clir.clearnlp.dependency.DEPTree;
import edu.emory.clir.clearnlp.srl.SRLTree;
import edu.emory.clir.clearnlp.util.arc.SRLArc;
import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.util.Pair;
import edu.stanford.nlp.util.Sets;
import nlp.features.AnswerExtractionFeaturizer;
import nlp.features.QCFeaturizationPipeline;
import nlp.features.SparseFeatureVector;
import nlp.learning.Scorer;
import nlp.qa.extractors.*;
import nlp.semantics.SemanticParser;

import java.io.File;
import java.io.FileReader;
import java.io.LineNumberReader;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static nlp.qa.QuestionsTaxonomy.*;

/****************************************************************
 * This is a manually set scorer of the features for extracting the answer
 */
class ManuallySetExtractingScorer implements Scorer {

    private SparseFeatureVector weightsVector;

    public ManuallySetExtractingScorer() {

        // define the weights here

        // under current manual approach, all those features represent some negative
        // effect according to which candidates with those features should be penalized
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

/****************************************************************
 */
public class ShortAnswerExtractor {

    private final static String APPOS = "appos";
    private final SemanticParser semanticParser;
    private final Scorer classifier;
    private final QCFeaturizationPipeline classificationFeaturizer;
    private final Scorer extractingScorer;
    private final AnswerExtractionFeaturizer extractingFeaturizer;

    // WordNet sense lists corresponding to UIUC answer types http://cogcomp.cs.illinois.edu/Data/QA/QC/definition.html
    // TOP level UIUC type key to a set of second level type keys with sense number values
    // Note there are no entries for ABBREVIATION, NUMERIC or DESCRIPTION types
    public static HashMap<String,HashMap<String,HashSet<String>>> senseMap = new HashMap<>();

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
     * Read in a mapping of UIUC answer types to WordNet 3.0 synsets
     */
    public static void readSenseMap() {

        LineNumberReader lr = null;
        try {
            String line;
            String filename = System.getenv("SIGMA_SRC") + "/../sumo/WordNetMappings/UIUC-WordNet-mapping.txt";
            File f = new File(filename);
            if (f == null) {
                System.out.println("Error in readSenseMap(): The file does not exist in " + filename);
                return;
            }
            FileReader r = new FileReader(filename);
            lr = new LineNumberReader(r);
            String topKey = "";
            String secondKey = "";
            HashSet<String> senseList = new HashSet<>();
            HashMap<String,HashSet<String>> lowMap = new HashMap<>();
            while ((line = lr.readLine()) != null) {
                if (line.matches("^[A-Z]+")) {
                    if (topKey != "") {
                        senseMap.put(topKey,lowMap);
                    }
                    topKey = line.trim();
                    lowMap = new HashMap<>();
                }
                else {
                    int tab = (line.indexOf('\t',1));
                    secondKey = line.substring(1,tab);
                    String values = line.substring(tab+1).trim();
                    String[] valuesAr = values.split(",");
                    HashSet<String> synsets = new HashSet<String>();
                    for (String s : valuesAr)
                        synsets.add(s.trim());
                    lowMap.put(secondKey,synsets);
                }
            }
        }
        catch (Exception ex) {
            System.out.println("Error in readSenseMap(): " + ex.getMessage());
            ex.printStackTrace();
        }
    }
    /****************************************************************
     * @return a single word extracted from the @param answer,
     *         as a short answer to the @param question
     */
    public String extract(String question, String answer) {

        //List<Pair<String, Double>> predicted = classifier.score(classificationFeaturizer.featurize(answer));

        // based on the class and semantic roles for both sentence and question - extract the extract
        DEPTree questionParsed = semanticParser.parse(question);
        DEPTree answerParsed = semanticParser.parse(answer);

        //DEPNode questionNode = findQuestionWord(questionParsed);
        DEPNode verbNode = findVerb(questionParsed);

        // get a coremap of the answer
        SemanticGraph depParse = classificationFeaturizer.extractor.parse(answer);

        SparseFeatureVector features = classificationFeaturizer.featurize(question);
        String questionCategory = features == null ? YESNO : classifier.score(features).get(0).first;

        System.out.println(questionCategory);

        DEPNode genericResult = genericExtract(questionParsed, answerParsed, questionCategory, verbNode);

        String decisionResult = extractWithDecisionTree(question, depParse, answerParsed, questionParsed,
                genericResult, questionCategory, verbNode);

        if (decisionResult != null)
            return decisionResult;
        if (genericResult != null)
            return genericResult.getWordForm();
        return null;
    }

    /****************************************************************
     */
    private String listOfIndexedWordToString(List<IndexedWord> result) {

        if (result == null || result.size() == 0)
            return "";
        return result.stream().map(IndexedWord::word).reduce((x, y) -> x + " " + y).get();
    }

    /****************************************************************
     * @return The short answer according to the decision tree rules
     */
    private String extractWithDecisionTree(String question, SemanticGraph answerGraph, DEPTree answerParsed,
                                           DEPTree questionParsed, DEPNode genericResult, String questionCategory, DEPNode verbNode) {

        List<IndexedWord> result = null;

        System.out.println("extractWithDecisionTree(): " + answerGraph);
        System.out.println("category: " + questionCategory);
        HashSet<String> synsets = new HashSet<>();
        String topCat = ""; // top level UIUC category
        String lowCat = ""; // second level UIUC category
        String[] cats = questionCategory.split(":");
        if (cats.length > 1) {
            topCat = cats[0];
            lowCat = cats[1];
            if (topCat != "" && senseMap.keySet().contains(topCat)) {
                HashSet<String> synsetSet = senseMap.get(topCat).get(lowCat);
                System.out.println("extractWithDecisionTree(): " + synsetSet);
            }
        }
        // ENTITY
        if (isCategoryOf(questionCategory, ENTITY)) {
            result = new RelationExtractor(APPOS).extract(answerGraph);
            if (result == null && isCategoryOf(questionCategory, ENTITY, _creative)) {
                result = new FirstCapSeqExtractor().extract(answerGraph);
            }
            System.out.println("extractWithDecisionTree(): WN match: " +
                    WordNetUtilities.isHyponymousWord(listOfIndexedWordToString(result),synsets));
        }

        // DESCRIPTION
        if (isCategoryOf(questionCategory, DESCRIPTION)) {
            if (isCategoryOf(questionCategory, DESCRIPTION, _description) || isCategoryOf(questionCategory, DESCRIPTION, _definition)) {
                result = new RelationExtractor(APPOS).extract(answerGraph);
                if (result == null) {
                    if (genericResult != null) {
                        return genericResult.getWordForm();
                    }
                    else {
                        result = new RelationExtractor("xcomp").extract(answerGraph);
                    }
                }
            }
        }

        // HUMAN
        if (isCategoryOf(questionCategory, HUMAN)) {
            if (isCategoryOf(questionCategory, _description)) {
                result = new HumDescExtractor().extract(answerGraph);
            }
            else {
                if (genericResult != null && genericResult.getNamedEntityTag().toLowerCase().equals("person")) {
                    String stringResult = genericResult.getWordForm();
                    System.out.println("extractWithDecisionTree(): WN match: " +
                            WordNetUtilities.isHyponymousWord(stringResult,synsets));
                    return stringResult;
                }
                //System.out.println("ShortAnswerExtractor.extractWithDecisionTree(): " + answerGraph);
                //System.out.println("ShortAnswerExtractor.extractWithDecisionTree(): " + question);
                List<IndexedWord> words = new HumanGenericExtractor().extract(answerGraph);
                if (words != null)
                    words = words.stream().filter(w ->
                        !question.contains(w.word())).collect(Collectors.toList());
                if (words != null && !words.isEmpty())
                    result = words;
            }
            System.out.println("extractWithDecisionTree(): WN match: " +
                    WordNetUtilities.isHyponymousWord(listOfIndexedWordToString(result),synsets));
        }

        // LOCATION
        if (isCategoryOf(questionCategory, LOCATION)) {
            result = new LocationExtractor(answerParsed,questionParsed).extract(answerGraph);
            System.out.println("extractWithDecisionTree(): WN match: " +
                    WordNetUtilities.isHyponymousWord(listOfIndexedWordToString(result),synsets));
        }

        // NUMERIC
        if (isCategoryOf(questionCategory, NUMERIC)) {
            if (isCategoryOf(questionCategory, NUMERIC, _count)) {
                result = new NumCountExtractor().extract(answerGraph);
            }
            if (isCategoryOf(questionCategory, NUMERIC, _date) ||
                    isCategoryOf(questionCategory, NUMERIC, _period)) {
                if (verbNode != null)
                    result = new NumDateExtractor(answerParsed, classificationFeaturizer.wordVecFeaturizer, verbNode).extract(answerGraph);
            }
            if (result == null) {
                result = new GenericNumExtractor().extract(answerGraph);
            }
        }

        // YES/NO
        if (isCategoryOf(questionCategory, YESNO)) {
            result = new YesNoExtractor().extract(answerGraph);
        }
        if (result != null) {
            result.sort(Comparator.comparingInt(IndexedWord::index));
        }
        if (result == null || result.size() == 0)
            return null;
        return result.stream().map(IndexedWord::word).reduce((x, y) -> x + " " + y).get();
    }

    /****************************************************************
     * makes a generic attempt to extract a short answer given semantic roles labeled graphs
     * and questions category
     */
    private DEPNode genericExtract(DEPTree questionParsed, DEPTree answerParsed, String questionCategory, DEPNode verbNode) {

        DEPNode matchedNode = matchSemantics(verbNode, answerParsed);

        if (answerParsed == null || answerParsed.getSRLTree(matchedNode) == null)
            return null;
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
     * @return semantic arcs
     */
    private List<SRLArc> getSemanticArcs(DEPNode node, DEPTree tree) {

        if (tree == null || node == null)
            return new ArrayList<>();
        SRLTree srlTree = tree.getSRLTree(node);
        if (srlTree == null)
            return new ArrayList<>();
        return srlTree.getArgumentArcList();
    }

    /****************************************************************
     * @return finds a node
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
     * @return find a verb
     */
    private DEPNode findVerb(DEPTree tree) {

        // find the question word and the nearest verb
        DEPNode questionWord = findQuestionWord(tree);

        if (questionWord != null && tree.toNodeArray() != null) {

            return Arrays.stream(tree.toNodeArray())
                    .filter(n -> n.getPOSTag().toLowerCase().startsWith("v"))
                    .sorted(Comparator.comparingInt(n -> Math.abs(questionWord.getID() - n.getID())))
                    .findFirst()
                    .orElse(null); //.get();
        }

        // if no question word, then just the first verb
        return Arrays.stream(tree.toNodeArray())
                .filter(n -> n.getPOSTag().toLowerCase().startsWith("v"))
                .sorted(Comparator.comparingInt(n -> n.getID()))
                .findFirst()
                .orElse(null); // .get();
    }

    /****************************************************************
     * @return finds a question word
     */
    private DEPNode findQuestionWord(DEPTree questionTree) {

        List<DEPNode> questionWords =
                Arrays.stream(questionTree.toNodeArray())
                        .filter(n -> QuestionFociExtractor.questionWords.contains(n.getWordForm().toLowerCase()))
                        .sorted(Comparator.comparingInt(n -> n.getID()))
                        .collect(Collectors.toList());

        if (!questionWords.isEmpty()) {
            return questionWords.get(0);
        }

        return null;
    }

    /****************************************************************
     * @return score which represents a match between two lists of semantic arcs
     */
    private double score(List<SRLArc> l1, List<SRLArc> l2) {

        HashSet<String> s1 = new HashSet<>(l1.stream().map(a -> a.getLabel()).collect(Collectors.toList()));
        HashSet<String> s2 = new HashSet<>(l2.stream().map(a -> a.getLabel()).collect(Collectors.toList()));

        return Sets.intersection(s1, s2).size() / (double) Sets.union(s1, s2).size();
    }

    /****************************************************************
     * @return Returns the head of the matched trees
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
