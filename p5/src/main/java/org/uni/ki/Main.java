package org.uni.ki;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import aima.core.logic.propositional.visitors.ImplicationElimination;
import aima.core.search.csp.Assignment;
import aima.core.search.csp.BacktrackingStrategy;
import aima.core.search.csp.CSP;
import aima.core.search.csp.Constraint;
import aima.core.search.csp.Domain;
import aima.core.search.csp.Variable;
import aima.core.search.csp.examples.MapCSP;

public class Main {

    public static void main(String[] args) {
        new Main();
    }

    Main() {
        List<Variable> vars = new ArrayList<>();
        List<Domain> doms = new ArrayList<>();

        Variable nat = new Variable("Nationalität");
        vars.add(nat);
        doms.add(new Domain(Arrays.asList("Engländer", "Schwede", "Däne", "Deutscher", "Norweger")));

        Variable color = new Variable("Hausfarbe");
        vars.add(color);
        doms.add(new Domain(Arrays.asList("Rot", "Grün", "Gelb", "Blau", "Weiß")));

        Variable pet = new Variable("Haustier");
        vars.add(pet);
        doms.add(new Domain(Arrays.asList("Hund", "Vogel", "Katze", "Pferd", "Zebra")));

        Variable drink = new Variable("Getränk");
        vars.add(drink);
        doms.add(new Domain(Arrays.asList("Tee", "Kaffee", "Milch", "Bier", "Wasser")));

        Variable zig = new Variable("Zigarette");
        vars.add(zig);
        doms.add(new Domain(Arrays.asList("Pall Mall", "Dunhill", "Blend", "Blue Master", "Prince")));

        Variable number = new Variable("Hausnummer");
        vars.add(number);
        doms.add(new Domain(Arrays.asList(1, 2, 3, 4, 5)));

        CSP csp = new CSP(vars);

        for (int i = 0; i < vars.size(); i++) {
            csp.setDomain(vars.get(i), doms.get(i));
        }

        List<Constraint> constraints = new ArrayList<>();

        constraints.add(new Constraints.AllDifferentConstraint(vars));

        constraints.add(new Constraints.SpecificBinaryConstraint(number, 1, nat, "Norweger"));
        constraints.add(new Constraints.SpecificBinaryConstraint(number, 3, drink, "Milch"));
        

        constraints.add(new Constraints.SpecificBinaryConstraint(nat, "Engländer", color, "Rot"));
        constraints.add(new Constraints.SpecificBinaryConstraint(nat, "Schwede", pet, "Hund"));
        constraints.add(new Constraints.NeighborConstraint(color, "Grün", color, "Weiß", null));
        constraints.add(new Constraints.SpecificBinaryConstraint(color, "Grün", drink, "Tee"));
 
        // this shit cant work
        // assignment hat einfach nicht alles was man braucht aka haus reihenfolge, demnach müsste hier eine eigene Assignment klasse gebaut werden
        // mit dieser eigenen Assigment klasse müsste dann auch CSP und andere Klassen umgebaut werden um das neue Assignment zu verwerden, diesen Aufwand werde ich nicht tun
    }
}