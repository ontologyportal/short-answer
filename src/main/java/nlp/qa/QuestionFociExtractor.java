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

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.semgraph.SemanticGraphCoreAnnotations;
import edu.stanford.nlp.util.CoreMap;
import edu.stanford.nlp.util.Pair;
import edu.stanford.nlp.util.PropertiesUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * This class extracts head words from a question later on to be used to featurize
 * the sentence for learning a classifier.
 *
 * http://www.adampease.org/professional/GlobalWordNet2016.pdf
 */
public class QuestionFociExtractor {

    public final static HashSet<String> questionWords = set("how what when where which who whom whose why");
    private final static HashSet<String> imperativeWords = set("define describe names name tell say give");
    private final static HashSet<String> muchMany = set("much many");
    private final static HashSet<String> typeBrandKind = set("type brand kind");
    private final static HashSet<String> entailment = set("am be was is were do does did 's are can called named known");
    private final StanfordCoreNLP pipeline;

    /****************************************************************
     * @return an instance of the foci extractor
     */
    public QuestionFociExtractor() {

        // Create the Stanford CoreNLP pipeline
        Properties props = PropertiesUtils.asProperties("annotators",
                "tokenize,ssplit,pos,lemma,ner,depparse",
                "ssplit.isOneSentence", "true",
                "tokenize.language", "en");

        this.pipeline = new StanfordCoreNLP(props);
    }

    /****************************************************************
     * @return a dependency parse of the text
     */
    public SemanticGraph parse(String text) {

        return annotateSentence(text).get(SemanticGraphCoreAnnotations.BasicDependenciesAnnotation.class);
    }

    /****************************************************************
     * @return an annotated first sentence from the text represented by an instance of CoreMap
     *         Note: assuming that @param text contains only one sentence
     */
    private CoreMap annotateSentence(String text) {

        Annotation doc = new Annotation(text);
        pipeline.annotate(doc);

        // Loop over sentences in the document
        return doc.get(CoreAnnotations.SentencesAnnotation.class).get(0);
    }

    /****************************************************************
     * Currently using a simple heuristic. Find a question word from a list, and
     * if not found, go for the imperative word.
     * @param graph The dependency graph
     */
    private IndexedWord extractQuestionWord(SemanticGraph graph) {

        // get all graph nodes
        List<IndexedWord> nodes = graph.getAllNodesByWordPattern(".*");
        nodes.sort(Comparator.comparingInt(n -> n.index()));

        Optional<IndexedWord> questionOption = nodes.stream().
                filter(w -> questionWords.contains(w.word().toLowerCase())).findFirst();
        if (questionOption.isPresent()) {
            return questionOption.get();
        }
        else {
            // look for the imperative
            Optional<IndexedWord> imperativeOption = nodes.stream().
                    filter(w -> imperativeWords.contains(w.word().toLowerCase())).findFirst();
            return imperativeOption.orElse(null);
        }
    }

    /****************************************************************
     * @return typed question foci terms with a question word
     */
    public QuestionFociTerms extractQuestionFociWordsWithType(String sentence) {

        final List<Pair<IndexedWord, String>> results = new ArrayList<>();

        SemanticGraph graph = parse(sentence);

        IndexedWord questionWord = extractQuestionWord(graph);

        if (questionWord == null) {
            return new QuestionFociTerms(results, "", "");
        }

        String questionWordString = questionWord.word().toLowerCase();
        String questionType = "NONE";

        Optional<IndexedWord> rootVerb = graph.getRoots().stream().filter(w -> w.tag().startsWith("V")).findFirst();

        List<IndexedWord> headWords = graph.getParentList(questionWord);

        if (!headWords.isEmpty()) {

            // different treatment of "how" question
            if (isHowQuestionWord(questionWord)) {

                headWords.forEach(w -> results.add(new Pair<>(w, "FOCUS")));

                // e.g. regular much, many
                if (headWords.size() == 1 && muchMany.contains(headWords.get(0).word().toLowerCase())) {

                    List<IndexedWord> parentList = graph.getParentList(headWords.get(0));
                    if (parentList.isEmpty()) {
                        questionType = headWords.get(0).word().toLowerCase().equals("much") ? "MUCH" : "SPAN";
                    }
                    else {
                        parentList.forEach(w -> results.add(new Pair<>(w, "QUANTITY")));
                    }
                }
            }
            else {

                // which, what...
                headWords.forEach(w -> results.add(new Pair<>(w, "FOCUS")));
            }
        }

        // type brand kind
        List<Pair<IndexedWord, String>> typeBrandResults = results.stream().filter(w ->
                typeBrandKind.contains(w.first.word().toLowerCase())).collect(Collectors.toList());

        typeBrandResults.forEach(w -> graph.getChildList(w.first).stream().
                filter(QuestionFociExtractor::isSuitableTerm).forEach(c -> results.add(new Pair<>(c, "FOCUS"))));

        // entailment
        if (rootVerb.isPresent() && entailment.contains(rootVerb.get().word().toLowerCase())) {
            graph.getChildList(rootVerb.get()).stream().filter(QuestionFociExtractor::isSuitableTerm).forEach(w ->
                    results.add(new Pair<>(w, "FOCUS")));
        }
        else {

            // children of question word
            graph.getChildList(questionWord).stream().filter(QuestionFociExtractor::isSuitableTerm).forEach(w ->
                    results.add(new Pair<>(w, "FOCUS")));

            // children of a verb which is a child of question word
            graph.getChildList(questionWord).stream().filter(c -> c.tag().startsWith("V")).forEach(v ->
                    graph.getChildList(v).stream().filter(QuestionFociExtractor::isSuitableTerm).forEach(w ->
                            results.add(new Pair<>(w, "FOCUS"))));
        }

        return new QuestionFociTerms(results.stream().filter(d -> d.first.word() != null).collect(Collectors.toList()),
                questionWords.contains(questionWordString) ? questionWordString : "imperative", questionType);
    }

    /****************************************************************
     * @return whether it is a how question
     */
    private static boolean isHowQuestionWord(IndexedWord word) {

        return word.word().toLowerCase().equals("how");
    }

    /****************************************************************
     * @return a set formed from splitting the string @param words
     */
    private static HashSet<String> set(String words) {

        return new HashSet<>(Arrays.asList(words.split(" ")));
    }

    /****************************************************************
     * @return whether this term should be extracted
     */
    private static boolean isSuitableTerm(IndexedWord d) {

        return d.tag() != null && (d.tag().startsWith("N") || d.tag().startsWith("F") || d.tag().startsWith("S") || d.tag().startsWith("J"));
    }
}
