package org.uni.ki;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;

import aima.core.learning.framework.Attribute;
import aima.core.learning.framework.DataSet;
import aima.core.learning.framework.DataSetSpecification;
import aima.core.learning.framework.Example;
import aima.core.learning.framework.NumericAttribute;
import aima.core.learning.framework.NumericAttributeSpecification;
import aima.core.learning.learners.DecisionTreeLearner;

public class New {
    static final String age = "age", income = "income", education = "education", canidate = "canidate";
    static final NumericAttributeSpecification ageAttr = new NumericAttributeSpecification(age),
            incomeAttr = new NumericAttributeSpecification(income),
            educationAttr = new NumericAttributeSpecification(education),
            canidateAttr = new NumericAttributeSpecification(canidate);

    static DataSet getData() {
        DataSetSpecification spec = new DataSetSpecification();

        spec.defineNumericAttribute(age); // 1 = >= 35, 2= < 35
        spec.defineNumericAttribute(income); // 1 = low, 2 = high
        spec.defineNumericAttribute(education);// abi = 1, bachelor = 2, master = 3
        spec.defineNumericAttribute(canidate); // O = 1, M = 2
        
        spec.setTarget(canidate);
        
        DataSet ds = new DataSet(spec);

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

        return ds;
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

    static class ID3 {

        DataSet ds;

        public static void main(String[] args) {
            ID3 notTheCar = new ID3();
            notTheCar.runID3();

        }

        void runID3() {
            ds = New.getData();

            DecisionTreeLearner dtl = new DecisionTreeLearner();
            dtl.train(ds); // Uses ID3
        }
    }

    static class CAL3 {

        public static void main(String[] args) {
            DataSet ds = getData();

            CAL3 cal = new CAL3(4, .7, ds.getAttributeNames());

            List<OwnExample> exampleData = OwnExample.fromDs(ds);
            
            for (OwnExample e : exampleData) {
                cal.learn(e);
            }
        }

        private final Node root = new Node();
        private final double S1, S2;
        private final List<String> attributes;

        public CAL3(double S1, double S2, List<String> attributes) {
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

                if (finalClass != null)
                    return;

                String k = e.target();
                classCounts.merge(k, 1, Integer::sum);
                int total = classCounts.values().stream().mapToInt(Integer::intValue).sum();

                if (total < S1)
                    return;

                var max = classCounts.entrySet().stream()
                        .max(Map.Entry.comparingByValue()).orElseThrow();
                double frac = (double) max.getValue() / total;

                if (frac >= S2) {
                    finalClass = max.getKey();
                    classCounts = null;
                    return;
                }

                String next = nextAttr(used, attrs);
                if (next == null)
                    return;

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
                if (finalClass != null)
                    return finalClass;
                if (classCounts.isEmpty())
                    return null;
                return classCounts.entrySet().stream()
                        .max(Map.Entry.comparingByValue()).get().getKey();
            }

            private static Set<String> addUsed(Set<String> used, String a) {
                Set<String> n = new HashSet<>(used);
                n.add(a);
                return n;
            }

            private static String nextAttr(Set<String> used, List<String> attrs) {
                for (String a : attrs)
                    if (!used.contains(a))
                        return a;
                return null;
            }
        }

        public interface Example {
            String target();

            double getAttr(String name);
        }

        static class OwnExample implements Example {
            String target = "";
            HashMap<String, Double> data = new HashMap<>();

            @Override
            public double getAttr(String name) {
                return data.get(name);
            }

            @Override
            public String target() {
                return target;
            }

            static List<OwnExample> fromDs(DataSet ds) {
                List<OwnExample> examples = new ArrayList<>();

                String target = ds.getTargetAttributeName();

                for (var e : ds.examples) {
                    OwnExample current = new OwnExample();
                    current.target = target;
                    for (String attr : ds.specification.getAttributeNames()) {
                        current.data.put(attr, e.getAttributeValueAsDouble(attr));
                    }
                    examples.add(current);
                }
                return examples;
            }
        }
    }
}