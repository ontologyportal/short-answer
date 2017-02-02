package nlp.qa;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.parser.nndep.DependencyParser;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.trees.GrammaticalStructure;
import edu.stanford.nlp.trees.TypedDependency;
import edu.stanford.nlp.util.CoreMap;
import edu.stanford.nlp.util.Pair;
import edu.stanford.nlp.util.PropertiesUtils;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class QuestionFociExtractor {

    public final static HashSet<String> questionWords = set("how what when where which who whom whose why");
    private final static HashSet<String> imperativeWords = set("define describe names name tell say give");
    //private final static HashSet<String> helperVerbs = set("am be was is were do does did 's are can");
    //private final static HashSet<String> entailment = set("called named known", helperVerbs);

    private DependencyParser parser;

    public QuestionFociExtractor(String parserPath) {
        this.parser = DependencyParser.loadFromModelFile(parserPath);
    }

    private Collection<TypedDependency> parse(String text) {

        CoreMap sentence = anotateSentence(text);
        GrammaticalStructure gs = parser.predict(sentence);

        return gs.typedDependencies();
    }

    /**
     * Currently using a simple heuristic. Find a question word from a list, and
     * if not found, go for the imperative word.
     * @param graph The dependency graph
     */
    private TypedDependency extractQuestionWord(Collection<TypedDependency> graph) {

        Optional<TypedDependency> questionOption = graph.stream().filter(d -> questionWords.contains(d.dep().word().toLowerCase())).findFirst();
        if (questionOption.isPresent()) {
            return questionOption.get();
        } else {
            // look for the imperative
            Optional<TypedDependency> imperativeOption = graph.stream().filter(d -> imperativeWords.contains(d.dep().word().toLowerCase())).findFirst();
            return imperativeOption.orElse(null);
        }
    }

    private CoreMap anotateSentence(String text) {

        // Create the Stanford CoreNLP pipeline
        // other possible annotators: "tokenize,ssplit,pos,lemma,depparse,natlog,openie"
        Properties props = PropertiesUtils.asProperties("annotators", "tokenize,ssplit,pos,lemma,ner");
        StanfordCoreNLP pipeline = new StanfordCoreNLP(props);

        Annotation doc = new Annotation(text);
        pipeline.annotate(doc);

        // Loop over sentences in the document
        return doc.get(CoreAnnotations.SentencesAnnotation.class).get(0);
    }

    public QuestionFociTerms extractQuestionFociWordsWithType(String sentence) {

        final List<Pair<IndexedWord, String>> results = new ArrayList<>();

        Collection<TypedDependency> dependencyGraph = parse(sentence);

        //dependencyGraph.forEach(d -> System.out.println(d + " : " + d.dep().backingLabel().tag()));

        TypedDependency questionWord = extractQuestionWord(dependencyGraph);

        if (questionWord == null) {
            return new QuestionFociTerms(results, "");
        }

        String questionWordString = questionWord.dep().word().toLowerCase();

        IndexedWord rootWord = GrammaticalStructure.getRoots(dependencyGraph).stream().findFirst().get().dep();

        Optional<TypedDependency> focus = dependencyGraph.stream().filter(d ->
                d.dep().equals(questionWord.dep())).findFirst();

        if (focus.isPresent()) {

            // different treatment of "how" question
            if (isHowQuestionWord(questionWord)) {

                // e.g. regular much, many

                results.add(new Pair<>(focus.get().gov(), "FOCUS"));

                Optional<TypedDependency> quantity = dependencyGraph.stream().filter(d -> d.dep().equals(focus.get().gov())).findFirst();

                // either QUANTITY or MUCH or SPAN
                if (quantity.isPresent()) {
                    if (rootWord.equals(quantity.get().gov())) {
                        IndexedWord word = focus.get().gov();
                        // / MUCH or SPAN
                        if (word.word().toLowerCase().equals("much")) {
                            results.add(new Pair<>(word, "MUCH"));
                        }
                        else {
                            results.add(new Pair<>(word, "SPAN"));
                        }
                    }
                    else {
                        results.add(new Pair<>(quantity.get().gov(), "QUANTITY"));
                    }
                }
            }
            else { // what, which ...

                Optional<TypedDependency> firstWordGov = dependencyGraph.stream().filter(d -> d.dep().equals(questionWord.dep()) && isSutableTerm(d, false)).findFirst();
                if (firstWordGov.isPresent()) {
                    results.add(new Pair<>(firstWordGov.get().gov(), "FOCUS"));
                    Optional<TypedDependency> secondWordGov = dependencyGraph.stream().filter(d -> d.dep().equals(firstWordGov.get().gov()) && isSutableTerm(d, false)).findFirst();
                    secondWordGov.ifPresent(td -> results.add(new Pair<>(td.gov(), "FOCUS")));
                } else {
                    Optional<TypedDependency> firstWordDep = dependencyGraph.stream().filter(d -> d.gov().equals(questionWord.dep()) && isSutableTerm(d)).findFirst();
                    if (firstWordDep.isPresent()) {
                        results.add(new Pair<>(firstWordDep.get().dep(), "FOCUS"));
                        Optional<TypedDependency> secondWordDep = dependencyGraph.stream().filter(d -> d.gov().equals(firstWordDep.get().dep()) && isSutableTerm(d)).findFirst();
                        secondWordDep.ifPresent(td -> results.add(new Pair<>(td.dep(), "FOCUS")));
                    }
                }
            }

            // if no feature by now, it is probably entailment
            // entailment like "is, was, called, ..."
            if (results.isEmpty() && rootWord.tag().startsWith("V")) {
                Stream<TypedDependency> rootChildren1 = dependencyGraph.stream().filter(d -> d.gov().equals(rootWord));
                rootChildren1.filter(QuestionFociExtractor::isSutableTerm).forEach(d -> results.add(new Pair<>(d.dep(), "FOCUS")));

//                Stream<TypedDependency> rootChildren2 = dependencyGraph.stream().filter(d -> d.gov().equals(rootWord));
//                rootChildren2.forEach(c -> dependencyGraph.stream().filter(d -> d.gov().equals(c.dep()) && isSutableTerm(d)).
//                        forEach(d -> results.add(new Pair<>(d.dep(), "FOCUS"))));
            }
        }

        if (results.stream().allMatch(w -> w.first.word() == null)) {
            System.out.println("All words are null in: " + sentence);
        }

        return new QuestionFociTerms(results.stream().filter(d -> d.first.word() != null).collect(Collectors.toList()),
            questionWords.contains(questionWordString)?questionWordString:"imperative");
    }

    private static boolean isHowQuestionWord(TypedDependency dep) {
        return dep.dep().word().toLowerCase().equals("how");
    }

    private static HashSet<String> set(String words, HashSet<String> other) {
        HashSet<String> result = set(words);
        result.addAll(other);
        return result;
    }

    private static HashSet<String> set(String words) {
        return new HashSet<>(Arrays.asList(words.split(" ")));
    }

    private static boolean isSutableTerm(TypedDependency d) {
        return isSutableTerm(d, true);
    }

    private static boolean isSutableTerm(TypedDependency d, boolean checkDep) {
        String tag = (checkDep?d.dep():d.gov()).backingLabel().tag();
        return tag != null && (tag.startsWith("N") || tag.startsWith("J") || tag.startsWith("F") || tag.startsWith("S"));
    }
}
