package nlp.qa;

public class QuestionWordNotFound extends RuntimeException {
    public QuestionWordNotFound() {
        super("Question word not found in the sentence!");
    }
}
