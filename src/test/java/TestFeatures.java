import nlp.features.SparseFeatureVector;
import org.junit.Assert;
import org.junit.Test;

public class TestFeatures {

    @Test
    public void testDot() {

        SparseFeatureVector v1 = new SparseFeatureVector();
        SparseFeatureVector v2 = new SparseFeatureVector();

        v1.add("f1");
        v1.add("f2");
        v1.add("f3");

        v2.add("f1", 3.0);
        v2.add("f3", -2.0);
        v2.add("f4", 1000.0);

        Assert.assertEquals(1.0, v1.dot(v2), 0.00000001);
    }

    @Test
    public void testMerge() {

        SparseFeatureVector v1 = new SparseFeatureVector();
        SparseFeatureVector v2 = new SparseFeatureVector();

        v1.add("f1");
        v1.add("f2");
        v1.add("f3");

        v2.add("f1", 3.0);
        v2.add("f3", -2.0);
        v2.add("f4", 1000.0);


        SparseFeatureVector v3 = new SparseFeatureVector();
        v3.add("f4");

        v1.mergeWith(v3);
        Assert.assertTrue(v1.getFeatures().containsKey("f1"));
        Assert.assertTrue(v1.getFeatures().containsKey("f2"));
        Assert.assertTrue(v1.getFeatures().containsKey("f3"));
        Assert.assertTrue(v1.getFeatures().containsKey("f4"));
        Assert.assertEquals(4, v1.getFeatures().size());
    }
}
