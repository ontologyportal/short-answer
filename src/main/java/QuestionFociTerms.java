import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.util.Pair;

import java.util.List;

public class QuestionFociTerms {

    List<Pair<IndexedWord, String>> termsWithTypes;
    String questionWord;

    public QuestionFociTerms(List<Pair<IndexedWord, String>> termsWithTypes, String questionWord) {
        this.termsWithTypes = termsWithTypes;
        this.questionWord = questionWord;
    }
}
