package org.uni.ki;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class Main { 
    public static void main(String[] args) throws Exception {
        queens();
    }
    public static void queens() throws Exception {
        Path log = Paths.get("log");
        Files.delete(log);
        
        Algo<QueensIndividual> a = new Algo<>(200, .015d, 1.4, () -> {QueensIndividual i = new QueensIndividual(); i.randomize(); return i;});

        a.sort();

        a.print(log);

        for (int i = 0; i < 6; i ++) {
            for (int j = 0; j < 5; j ++) {
                a.generation();
            }

            a.sort();

            a.print(log);
        }
    }
    public static void color() throws Exception {
        ColorIndividual s = ColorIndividual.fromArr(new Integer[]{1,1,1,1,1,1});
        ColorIndividual s2 = ColorIndividual.fromArr(new Integer[]{3,3,3,3,3,3});

        //System.out.println(s.getFitness());
        //System.out.println(s);

        //s = s.mutate(.5d);

        
        //System.out.println(s.getFitness());
        //System.out.println(s);

        //System.out.print(s.crossOver(s2));

        Algo<ColorIndividual> a = new Algo<ColorIndividual>(20, 0.06d, 1d,  () -> {ColorIndividual i = new ColorIndividual();i.randomize(); return i;});

        Path log = Paths.get("log");
        Files.delete(log);

        System.out.println("Unordered");
        a.print(log);
        
        System.out.println("================================================================\nOrdered:");

        a.print(log);

        a.generation();
        
        a.sort();

        a.print(log);
        
        a.generation();
        
        a.sort();

        a.print(log);
    }
    
    interface Individual {
        double getFitness();
        <I extends Individual> I crossOver(I b) throws Exception;
        <I extends Individual> I mutate(double pMut);
        void randomize();
        boolean isSolved();
    }

    static class ColorIndividual implements Individual {
        public final static Integer[] COLORS = {1,2,3,4,5};
        public final static List<Integer> c = Arrays.asList(COLORS);
        public final static char[] COUNTRIES = {'a', 'b', 'c', 'd', 'e', 'f'};
        public final static Border[] BORDERS = {
            new Border('a','b'),
            new Border('a','c'),
            new Border('b','c'),
            new Border('b','d'),
            new Border('c','d'),
            new Border('d','e'),
        };

        public Integer[] code; 

        static class Border {
            char a, b;

            Border(char a, char b) {
                this.a = a;
                this.b = b;
            }
        }

        int getConflicts() {
            int conflicts = 0;
            for (int i = 0; i < code.length; i ++) {
                char country = COUNTRIES[i];

                for (Border b : BORDERS) {
                    if (b.a != country && b.b != country) continue;
                    
                    if (code[indexFromCountry(b.a)] == code[indexFromCountry(b.b)]) conflicts ++;
                } 
            }
            
            return conflicts;
        }

        int indexFromCountry(char c) {
            switch (c) {
                case 'a':
                    return 0;
                case 'b':
                    return 1;
                case 'c':
                    return 2;
                case 'd':
                    return 3;
                case 'e':
                    return 4;
                case 'f':
                    return 5; 
                default:
                    throw new IllegalStateException("bad letter " + c);
            }
        }

        int getColorsUsed() {
            HashSet<Integer> used = new HashSet<Integer>();
            for (Integer i : code) {
                used.add(i);
            }
            return used.size();
        }

        public double getFitness() {
            //System.out.println(getColorsUsed());
            return 1d /  (getConflicts() * 10d + getColorsUsed() ); 
        }

        public ColorIndividual crossOver(ColorIndividual b) {
            Integer[] ch = new Integer[6];
            
            // from 1 to 4 keep atleast 1 from each parent
            int split = new Random().nextInt(6-1)+1;

            for (int i = 0; i < 6; i ++) {
                ch[i] = i < split ? code[i] : b.code[i]; 
            }

            return fromArr(ch);
        }

        public ColorIndividual mutate(double pMut) {
            Integer[] newCode = Arrays.copyOf(code, 6);
            for (int i = 0; i < code.length; i ++) {
                newCode[i] = Math.random() < pMut ? new Random().nextInt(COLORS.length) : code[i];
            }

            return fromArr(newCode);
        } 

        static ColorIndividual fromArr(Integer[] code) {
            ColorIndividual s = new ColorIndividual();
            if (code.length != 6) {
                throw new IllegalStateException("bad length " + code.length);
            }
            s.code = Arrays.copyOf(code, 6);
            return s;
        }

        public String toString() {
            String s = "[ ";
            for (int i : code) {
                s = s + String.valueOf(i) + " ";
            }
            s = s + "]";

            return s;
        }

        ColorIndividual random() {
            return new ColorIndividual();
        }

        public void randomize() {
            code = new Integer[6];
            for (int i = 0; i < code.length; i ++) {
                code[i] = new Random().nextInt(5)+1;
            }
        }

        @Override
        public <I extends Individual> I crossOver(I b) {
            if (b instanceof ColorIndividual) {
                return (I) crossOver((ColorIndividual) b);
            }
            throw new IllegalArgumentException();
        }

        @Override
        public boolean isSolved() {
            return getConflicts() == 0;
        }
    }

    static class Algo<I extends Individual> {
        ArrayList<I> Population;
        int poolSize;
        double pMut, pCross;
    
        public Algo(int pop, double pMut, double pCross,  Supplier<I> sup) {
            poolSize = pop;
            this.pMut = pMut;
            this.pCross = pCross;
            Population = new ArrayList<>();
            for (int i = 0; i < pop; i ++) {
                 Population.add(sup.get());
            }
        }

        void sort() {
            Population.sort(Comparator.comparingDouble(I::getFitness));
            Collections.reverse(Population);
        }

        void print(Path to) {
            List<String> sb = new LinkedList<>();
            for (I i : Population) {
                sb.add(String.valueOf(i.getFitness()));
                sb.add(String.valueOf(i));
                //System.out.println(i.getFitness());
                //System.out.println(i);
            };
            sb.add("===");
            if (to != null) {
                try {
                    Files.write(to, sb, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        void rouletteSelection() {
            System.out.print("Running Roulette Selection from " + Population.size());
            final double[] currentOddsPtr = {0};
            double maxOdds = pCross;
            final double step = maxOdds / Population.size();

            Population.removeIf((I i) ->  {
                currentOddsPtr[0] += step;
                return Math.random() < currentOddsPtr[0];
            });

            System.out.println(" to " + Population.size());
        }

        void repopulate() throws Exception {
            double oddsOfMating = 1 - Population.size() / poolSize;

            int startSize = Population.size();
            while (true) {
                for (int i = 0; i < startSize; i ++) {
                    if (Population.size() == poolSize) return;
                    if (Math.random() < oddsOfMating) {
                        Population.add(Population.get(i).crossOver(Population.get(new Random().nextInt(startSize))));
                    }
                }
            }
        }

        void mutate() {
            System.out.println("mutanting pool");
            Population = (ArrayList<I>) Population.stream().map((I i) -> i.mutate(pMut)).collect(Collectors.toCollection(ArrayList::new));
        }

        void generation() throws Exception {
            System.out.println("sorted pop");
            sort();

            if (Population.get(0).isSolved()) {
                System.out.println("is Solved");
            }

            rouletteSelection();

            repopulate();

            mutate();
        }
    }

    static class QueensIndividual implements Individual {
        Integer[] code;

        @Override
        public double getFitness() {
            int col = 0;
            for (int i = 0; i < 8; i++) {
                for (int j = 0; j < 8; j ++) {
                    if (j == i) continue;
                        if (code[i] == code[j]) col ++;
                        if (i - j == code[i] - code[j]) col ++;
                        if (i + j == code[i] + code[j]) col ++;
                }
            }
            
            return 100 - col;
        }

        QueensIndividual() {
            code = new Integer[8];
        }

        QueensIndividual fromArr(Integer[] code) {
            QueensIndividual i = new QueensIndividual();
            i.code = code;
            return i;
        }

        QueensIndividual crossOver(QueensIndividual b) throws Exception {
            if (b == null) {
                throw new Exception();
            }
            Integer[] ch = new Integer[8];
            
            int split = new Random().nextInt(8-1)+1;

            for (int i = 0; i < 8; i ++) {
                ch[i] = i < split ? code[i] : b.code[i]; 
            }

            return fromArr(ch);
            
        }

        @Override
        public <I extends Individual> I crossOver(I b) throws Exception {
            if (b instanceof QueensIndividual) {
                return (I) crossOver((QueensIndividual) b);
            }
            throw new IllegalArgumentException();
        }

        @Override
        public <I extends Individual> I mutate(double pMut) {
            Integer[] newCode = Arrays.copyOf(code, 8);
            for (int i = 0; i < code.length; i ++) {
                newCode[i] = Math.random() < pMut ? new Random().nextInt(8) : code[i];
            }

            return (I) fromArr(newCode);
        }

        @Override
        public void randomize() {
            for (int i = 0; i < 8; i ++) {
                code[i] = new Random().nextInt(8);
            }
        }

        @Override
        public boolean isSolved() {
            return getFitness() == 100;
        }
        
        public String toString() {
            String s = "[ ";
            for (int i : code) {
                s = s + String.valueOf(i) + " ";
            }
            s = s + "]";

            return s;
        }
    }
}
