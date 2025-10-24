package org.uni.ki;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.function.Supplier;

public class Main { 
    public static void main(String[] args) {
        FlagIndividual s = FlagIndividual.fromArr(new Integer[]{1,1,1,1,1,1});
        FlagIndividual s2 = FlagIndividual.fromArr(new Integer[]{3,3,3,3,3,3});

        //System.out.println(s.getFitness());
        //System.out.println(s);

        //s = s.mutate(.5d);

        
        //System.out.println(s.getFitness());
        //System.out.println(s);

        //System.out.print(s.crossOver(s2));

        Algo<FlagIndividual> a = new Algo<FlagIndividual>(10, () -> {FlagIndividual i = new FlagIndividual();i.randomize(); return i;});

        System.out.println("Unordered");
        a.print();
        
        System.out.println("================================================================\nOrdered:");

        a.sort();

        a.print();
    }
    
    interface Individual {
        double getFitness();
        <I extends Individual> I crossOver(I b);
        <I extends Individual> I mutate(double pMut);
        void randomize();
    }

    static class FlagIndividual implements Individual {
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
            System.out.println(getColorsUsed());
            return 1d /  (getConflicts() * 10d + getColorsUsed() ); 
        }

        public FlagIndividual crossOver(FlagIndividual b) {
            Integer[] ch = new Integer[6];
            
            // from 1 to 4 keep atleast 1 from each parent
            int split = new Random().nextInt(6-1)+1;

            for (int i = 0; i < 6; i ++) {
                ch[i] = i < split ? code[i] : b.code[i]; 
            }

            return fromArr(ch);
        }

        public FlagIndividual mutate(double pMut) {
            Integer[] newCode = Arrays.copyOf(code, 6);
            for (int i = 0; i < code.length; i ++) {
                newCode[i] = Math.random() < pMut ? new Random().nextInt(COLORS.length) : code[i];
            }

            return fromArr(newCode);
        } 

        static FlagIndividual fromArr(Integer[] code) {
            FlagIndividual s = new FlagIndividual();
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

        FlagIndividual random() {
            return new FlagIndividual();
        }

        public void randomize() {
            code = new Integer[6];
            for (int i = 0; i < code.length; i ++) {
                code[i] = new Random().nextInt(5)+1;
            }
        }

        @Override
        public <I extends Individual> I crossOver(I b) {
            return (I) crossOver((FlagIndividual) b);
        }
    }

    static class Algo<I extends Individual> {
        ArrayList<I> Population;

        public Algo(int pop, Supplier<I> sup) {
            Population = new ArrayList<>();
            for (int i = 0; i < pop; i ++) {
                 Population.add(sup.get());
            }
        }

        void sort() {
            Population.sort(Comparator.comparingDouble(I::getFitness));
            Collections.reverse(Population);
        }

        void print() {
            Population.forEach((I i) -> {
                System.out.println(i.getFitness());
                System.out.println(i);
            });
        }
    }
}
