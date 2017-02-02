package nlp.qa;

import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.util.Pair;

import java.util.List;

public class QuestionFociTerms {

    public List<Pair<IndexedWord, String>> termsWithTypes;
    public String questionWord;

    public QuestionFociTerms(List<Pair<IndexedWord, String>> termsWithTypes, String questionWord) {
        this.termsWithTypes = termsWithTypes;
        this.questionWord = questionWord;
    }
}
