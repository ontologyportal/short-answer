/*
 *  This code is copyright CloudMinds 2017.
 *
 *  Author: Yan Virin jan.virin@gmail.com
 *
 *  This software is released under the GNU Public License <http://www.gnu.org/copyleft/gpl.html>.
 *  Please cite the following article in any publication with references:
 *  Pease A., and Benzmüller C. (2013). Sigma: An Integrated Development Environment for Logical Theories. AI Communications 26, pp79-97.
 */

import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.util.PropertiesUtils;
import nlp.qa.QuestionFociExtractor;
import nlp.qa.extractors.HumDescExtractor;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;
import java.util.stream.Collectors;

public class TestExtractors {

    @Test
    public void testHumanDescExtractor() {

        String sentence = "John Lenon, the engineer, was the one";
        String sentence2 = "Great and powerful person John Lenon";
        String sentence3 = "Jphn Lenon is a good friend";

        QuestionFociExtractor extractor = new QuestionFociExtractor();

        SemanticGraph parse = extractor.parse(sentence3);

        HumDescExtractor humDescExtractor = new HumDescExtractor();
        List<String> result = humDescExtractor.extract(extractor.parse(sentence)).stream().map(w -> w.word()).collect(Collectors.toList());
        List<String> result2 = humDescExtractor.extract(extractor.parse(sentence2)).stream().map(w -> w.word()).collect(Collectors.toList());
        List<String> result3 = humDescExtractor.extract(extractor.parse(sentence3)).stream().map(w -> w.word()).collect(Collectors.toList());

        Assert.assertEquals("[the, engineer]", result.toString());
        Assert.assertEquals("[powerful, person]", result2.toString());
        Assert.assertEquals("[a, good, friend]", result3.toString());

    }

}
