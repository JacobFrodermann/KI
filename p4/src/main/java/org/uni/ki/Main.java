package org.uni.ki;

import aima.core.learning.framework.Learner;
import aima.core.learning.framework.Attribute;
import aima.core.learning.framework.DataSet;
import aima.core.learning.framework.DataSetSpecification;
import aima.core.learning.framework.Example;
import aima.core.learning.framework.NumericAttribute;
import aima.core.learning.framework.NumericAttributeSpecification;
import aima.core.learning.inductive.ConstantDecisonTree;
import aima.core.learning.inductive.DecisionTree;
import aima.core.learning.learners.*;
import aima.core.search.framework.Node;
import aima.core.util.Util;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;



public class Main {

    DataSetSpecification spec = new DataSetSpecification();

    DataSet ds;

    static final String age = "age", income = "income", education = "education", canidate = "canidate";
    static final NumericAttributeSpecification ageAttr = new NumericAttributeSpecification(age),
            incomeAttr = new NumericAttributeSpecification(income),
            educationAttr = new NumericAttributeSpecification(education),
            canidateAttr = new NumericAttributeSpecification(canidate);

    public static void main(String[] args) {
        new Main();
    }

    Main() {

        spec.defineNumericAttribute(age); // 1 = >= 35, 2= < 35
        spec.defineNumericAttribute(income); // 1 = low, 2 = high
        spec.defineNumericAttribute(education);// abi = 1, bachelor = 2, master = 3
        spec.defineNumericAttribute(canidate); // O = 1, M = 2

        spec.setTarget(canidate);

        ds = new DataSet(spec);

        List<Example> rawData = new ArrayList<>();

        rawData.add(helper(0, 2, 1, 1));
        rawData.add(helper(2, 1, 3, 1));
        rawData.add(helper(1, 2, 2, 2));
        rawData.add(helper(1, 1, 1, 2));
        rawData.add(helper(1, 2, 3, 1));
        rawData.add(helper(2, 2, 2, 1));
        rawData.add(helper(2, 1, 1, 2));

        for (var entry : rawData) {
            ds.add(entry);
        }

        DecisionTreeLearner dtl = new UsefullDTL();
        dtl.train(ds);

    }

    static Example helper(int a, int inc, int edu, int can) {

        Hashtable<String, Attribute> d = new Hashtable<>();
        d.put(age, (Attribute) new NumericAttribute(a, ageAttr));
        d.put(income, (Attribute) new NumericAttribute(inc, incomeAttr));
        d.put(education, (Attribute) new NumericAttribute(edu, educationAttr));
        d.put(canidate, (Attribute) new NumericAttribute(can, canidateAttr));

        Example e = new Example(d, new NumericAttribute(can, canidateAttr));

        return e;
    }

    private class UsefullDTL extends DecisionTreeLearner {

        private DecisionTree tree;

        public void trainID3(DataSet ds) {
            List<String> attributes = ds.getNonTargetAttributes();
            this.tree = ID3(ds, attributes,
                    new ConstantDecisonTree(null));
        }

        public void traincal3(DatasetWithSplitInformation ds) {
            List<String> attributes = ds.getNonTargetAttributes();
            this.tree = CAL3(ds, attributes,
                    new ConstantDecisonTree(null));
        }

        private DecisionTree ID3(DataSet ds,
                List<String> attributeNames, ConstantDecisonTree defaultTree) {
            if (ds.size() == 0) {
                return defaultTree;
            }
            if (allExamplesHaveSameClassification(ds)) {
                return new ConstantDecisonTree(ds.getExample(0).targetValue());
            }
            if (attributeNames.size() == 0) {
                return majorityValue(ds);
            }
            String chosenAttribute = chooseAttribute(ds, attributeNames);

            DecisionTree tree = new DecisionTree(chosenAttribute);
            ConstantDecisonTree m = majorityValue(ds);

            List<String> values = ds.getPossibleAttributeValues(chosenAttribute);
            for (String v : values) {
                DataSet filtered = ds.matchingDataSet(chosenAttribute, v);
                List<String> newAttribs = Util.removeFrom(attributeNames,
                        chosenAttribute);
                DecisionTree subTree = ID3(filtered, newAttribs, m);
                tree.addNode(v, subTree);

            }

            return tree;
        }

        private DecisionTree CAL3(DatasetWithSplitInformation ds,
                List<String> attributeNames, ConstantDecisonTree defaultTree) {
            if (ds.size() == 0) {
                return defaultTree;
            }
            if (allExamplesHaveSameClassification(ds)) {
                return new ConstantDecisonTree(ds.getExample(0).targetValue());
            }
            if (attributeNames.size() == 0) {
                return majorityValue(ds);
            }


            

            return null;

        }

        private ConstantDecisonTree majorityValue(DataSet ds) {
            Learner learner = new MajorityLearner();
            learner.train(ds);
            return new ConstantDecisonTree(learner.predict(ds.getExample(0)));
        }

        private String chooseAttribute(DataSet ds, List<String> attributeNames) {
            double greatestGain = 0.0;
            String attributeWithGreatestGain = attributeNames.get(0);
            for (String attr : attributeNames) {
                double gain = ds.calculateGainFor(attr);
                if (gain > greatestGain) {
                    greatestGain = gain;
                    attributeWithGreatestGain = attr;
                }
            }

            return attributeWithGreatestGain;
        }

        //
        private String chooseAttributeWithBestGainRatio(DatasetWithSplitInformation ds, List<String> attributeNames) {
            double greatestGainRatio = Integer.MIN_VALUE;
            String attributeWithGreatestGain = ds.getAttributeNames().get(0);

            for (String attr : attributeNames) {
                double gain = ds.calculateGainFor(attr);
                double splitInfo = ds.getSplitInfoFor(attr);
                if (gain / splitInfo > greatestGainRatio) {
                    greatestGainRatio = gain;
                    attributeWithGreatestGain = attr;
                }
            }

            return attributeWithGreatestGain;
        }

        private boolean allExamplesHaveSameClassification(DataSet ds) {
            String classification = ds.getExample(0).targetValue();
            Iterator<Example> iter = ds.iterator();
            while (iter.hasNext()) {
                Example element = iter.next();
                if (!(element.targetValue().equals(classification))) {
                    return false;
                }

            }
            return true;
        }
    }

    private class DatasetWithSplitInformation extends DataSet {
        public double getSplitInfoFor(String attr) {
            HashMap<Double, Integer> counts = new HashMap<>();

            int total = this.getNonTargetAttributes().size();

            for (Example e : this.examples) {
                double val = e.getAttributeValueAsDouble(attr);

                if (counts.containsKey(val)) {
                    counts.put(val, counts.get(val)+1);
                } else {
                    counts.put(val, 0);
                }                
            }

            double splitInfo = 0;
            for (var entry : counts.entrySet()) {
                double p = entry.getValue() / total; 
                if (p != 0) {
                    splitInfo -= p * log(2, p);
                }
            } 
            return splitInfo;
        }
    }

    static double log(int x, double base) {
        return Math.log(x) / Math.log(base);
    }

class CAL3Numeric {
    private final Node root = new Node();
    private final double S1, S2;
    private final List<String> attributes;

    public CAL3Numeric(double S1, double S2, List<String> attributes) {
        this.S1 = S1;
        this.S2 = S2;
        this.attributes = attributes;
    }

    public void learn(Example e) {
        root.learn(e, new HashSet<>(), S1, S2, attributes);
    }

    public String predict(Example e) {
        return root.predict(e);
    }

    static class Node {
        boolean isLeaf = true;
        String finalClass;
        HashMap<String, Integer> classCounts = new HashMap<>();

        // for internal nodes
        String splitAttr;
        HashMap<Double, Node> children;

        void learn(Example e, Set<String> used, double S1, double S2, List<String> attrs) {
            if (!isLeaf) {
                double val = e.getAttr(splitAttr);
                children.computeIfAbsent(val, k -> new Node())
                        .learn(e, addUsed(used, splitAttr), S1, S2, attrs);
                return;
            }

            if (finalClass != null) return;

            String k = e.target();
            classCounts.merge(k, 1, Integer::sum);
            int total = classCounts.values().stream().mapToInt(Integer::intValue).sum();

            if (total < S1) return;

            var max = classCounts.entrySet().stream()
                    .max(Map.Entry.comparingByValue()).orElseThrow();
            double frac = (double) max.getValue() / total;

            if (frac >= S2) {
                finalClass = max.getKey();
                classCounts = null;
                return;
            }

            String next = nextAttr(used, attrs);
            if (next == null) return;

            splitAttr = next;
            isLeaf = false;
            children = new HashMap<>();

            double val = e.getAttr(splitAttr);
            Node c = new Node();
            c.classCounts.put(k, 1);
            children.put(val, c);
        }

        String predict(Example e) {
            if (!isLeaf) {
                Node c = children.get(e.getAttr(splitAttr));
                return c == null ? null : c.predict(e);
            }
            if (finalClass != null) return finalClass;
            if (classCounts.isEmpty()) return null;
            return classCounts.entrySet().stream()
                    .max(Map.Entry.comparingByValue()).get().getKey();
        }

        private static Set<String> addUsed(Set<String> used, String a) {
            Set<String> n = new HashSet<>(used);
            n.add(a);
            return n;
        }

        private static String nextAttr(Set<String> used, List<String> attrs) {
            for (String a : attrs) if (!used.contains(a)) return a;
            return null;
        }
    }

    public interface Example {
        String target();
        double getAttr(String name);
    }
}
}