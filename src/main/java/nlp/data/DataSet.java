package nlp.data;

import edu.stanford.nlp.util.Pair;
import nlp.features.SparseFeatureVector;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class DataSet {

    public Labels labels;
    public List<Pair<String, String>> rawDataPoints = new ArrayList<>();
    public List<Pair<Integer, SparseFeatureVector>> dataPoints = new ArrayList<>();

    public Pair<DataSet, DataSet> split(double part) {

        Collections.shuffle(rawDataPoints, new Random(0));
        Collections.shuffle(dataPoints, new Random(0));

        DataSet part1 = new DataSet();
        DataSet part2 = new DataSet();

        int lastIndex = (int)(rawDataPoints.size()*part);
        for (int i = 0; i < rawDataPoints.size(); i++) {
            DataSet temp = i < lastIndex ? part1 : part2;
            temp.rawDataPoints.add(rawDataPoints.get(i));
            temp.dataPoints.add(dataPoints.get(i));
            temp.labels = labels;
        }

        return new Pair<>(part1, part2);
    }
}
