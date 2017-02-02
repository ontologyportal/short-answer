package nlp.features;

import edu.emory.clir.clearnlp.dependency.DEPNode;
import edu.emory.clir.clearnlp.util.arc.SRLArc;

import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class AnswerExtractionFeaturizer {

    public static final String EXTRACTION = "EXTRACTION";

    public static final String CLASS = "CLASS";

    private static final Pattern ARGM_PAT  = Pattern.compile("A[0-9]+");
    public static final String ARGM = "ARGM";
    public static final String SEMANTIC_NONMATCH = "SEMANTIC_NONMATCH";
    public static final String PREP  = "prep";
    public static final String WORD  = "WORD";
    public static final String LEMMA = "LEMMA";
    public static final String LOWERCASE = "LOWERCASE";
    public static final String WH        = "W";
    public static final String ADVERB    = "RB";

    public static String featureName(String name) {
        return String.format("%s_%s", EXTRACTION, name);
    }

    private boolean isHumanOrEntity(String questionCategory) {
        return questionCategory.startsWith("ENTY") || questionCategory.startsWith("HUM");
    }

    public SparseFeatureVector featurize(SRLArc candidate, List<SRLArc> original, String questionCategory) {

        SparseFeatureVector result = new SparseFeatureVector();

        // set the gross class as a feature
        result.add(featureName(String.format("%s_%s", CLASS, questionCategory)));

        String candidateLabel = candidate.getLabel();

        if (isHumanOrEntity(questionCategory)) {

            // semantic role
            if (!ARGM_PAT.matcher(candidateLabel).find()) result.add(featureName(ARGM));

            // preposition
            if (candidate.getNode().getLabel().startsWith(PREP)) result.add(featureName(PREP));

            // lowercase feature
            if (candidate.getNode().getWordForm().toLowerCase().equals(candidate.getNode().getWordForm()))
                result.add(featureName(LOWERCASE));
        }

        Set<DEPNode> originalNodes = original.stream().map(a -> a.getNode()).collect(Collectors.toSet());

        // semantic non match
        if (!original.stream().map(a -> a.getLabel()).anyMatch(l -> l.contains(candidateLabel))) {
            result.add(featureName(SEMANTIC_NONMATCH));
        }

        // same word feature
        if (originalNodes.stream().map(n -> n.getWordForm()).anyMatch(w -> w.equals(candidate.getNode().getWordForm()))) result.add(featureName(WORD));

        // same lemma feature
        if (originalNodes.stream().map(n -> n.getLemma()).anyMatch(w -> w.equals(candidate.getNode().getLemma()))) result.add(featureName(LEMMA));


        // WH quantifier pos tag feature
        if (candidate.getNode().getPOSTag().startsWith(WH)) result.add(featureName(WH));

        // adverb pos tag feature
        if (candidate.getNode().getPOSTag().startsWith(ADVERB)) result.add(featureName(ADVERB));

        return result;
    }
}
