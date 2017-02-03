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
