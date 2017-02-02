package nlp.semantics;

import edu.emory.clir.clearnlp.component.mode.dep.AbstractDEPParser;
import edu.emory.clir.clearnlp.component.mode.dep.DEPConfiguration;
import edu.emory.clir.clearnlp.component.mode.morph.AbstractMPAnalyzer;
import edu.emory.clir.clearnlp.component.mode.ner.AbstractNERecognizer;
import edu.emory.clir.clearnlp.component.mode.pos.AbstractPOSTagger;
import edu.emory.clir.clearnlp.component.mode.srl.AbstractSRLabeler;
import edu.emory.clir.clearnlp.component.mode.srl.SRLConfiguration;
import edu.emory.clir.clearnlp.component.utils.GlobalLexica;
import edu.emory.clir.clearnlp.component.utils.NLPUtils;
import edu.emory.clir.clearnlp.dependency.DEPTree;
import edu.emory.clir.clearnlp.tokenization.AbstractTokenizer;
import edu.emory.clir.clearnlp.util.lang.TLanguage;

import java.util.ArrayList;
import java.util.List;

public class SemanticParser {

    private final static String brownClustersXZ = "brown-rcv1.clean.tokenized-CoNLL03.txt-c1000-freq1.txt.xz";
    private final static String nerGazeteerXZ = "general-en-ner-gazetteer.xz";
    private final static String posModelXZ = "general-en-pos.xz";
    private final static String depModelXZ = "general-en-dep.xz";
    private final static String srlModelXZ = "general-en-srl.xz";
    private final static String nerModelXZ = "general-en-ner.xz";

    private final static String ROOT = "ROOT";
    private final static TLanguage language = TLanguage.ENGLISH;
    private final static SRLConfiguration srlConf = new SRLConfiguration(4, 3);

    private final AbstractTokenizer tokenizer;
    private final AbstractMPAnalyzer morph;
    private final AbstractPOSTagger pos;
    private final AbstractDEPParser dep;
    private final AbstractSRLabeler srl;
    private final AbstractNERecognizer ner;

    public SemanticParser() {
        List<String> paths = new ArrayList<>();
        paths.add(brownClustersXZ);

        GlobalLexica.initDistributionalSemanticsWords(paths);
        GlobalLexica.initNamedEntityDictionary(nerGazeteerXZ);

        // initialize statistical models
        this.tokenizer = NLPUtils.getTokenizer(language);
        this.morph     = NLPUtils.getMPAnalyzer(language);
        this.pos = NLPUtils.getPOSTagger   (language, posModelXZ);
        this.dep = NLPUtils.getDEPParser   (language, depModelXZ, new DEPConfiguration(ROOT));
        this.srl = NLPUtils.getSRLabeler   (language, srlModelXZ, srlConf);
        this.ner = NLPUtils.getNERecognizer(language, nerModelXZ);
    }

    public DEPTree parse(String sentence) {
        DEPTree tree = new DEPTree(this.tokenizer.tokenize(sentence));
        pos.process(tree);
        morph.process(tree);
        dep.process(tree);
        srl.process(tree);
        ner.process(tree);
        return tree;
    }






}
