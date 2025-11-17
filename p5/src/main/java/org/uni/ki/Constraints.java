package org.uni.ki;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import aima.core.search.csp.Assignment;
import aima.core.search.csp.Constraint;
import aima.core.search.csp.Variable;

public class Constraints {
    public static class SpecificBinaryConstraint implements Constraint {
        private Variable var1;
        private Variable var2;
        private Object expectedValue1;
        private Object expectedValue2;
        private List<Variable> scope;

        public SpecificBinaryConstraint(Variable var1, Object expectedValue1,
                Variable var2, Object expectedValue2) {
            this.var1 = var1;
            this.var2 = var2;
            this.expectedValue1 = expectedValue1;
            this.expectedValue2 = expectedValue2;
            this.scope = List.of(var1, var2);
        }

        @Override
        public List<Variable> getScope() {
            return scope;
        }

        @Override
        public boolean isSatisfiedWith(Assignment assignment) {
            Object val1 = assignment.getAssignment(var1);
            Object val2 = assignment.getAssignment(var2);

            // Wenn beide Werte gesetzt sind, müssen sie den erwarteten Werten entsprechen
            if (val1 != null && val2 != null) {
                return val1.equals(expectedValue1) && val2.equals(expectedValue2);
            }

            // Wenn nur einer gesetzt ist, prüfe ob er konsistent ist
            if (val1 != null && !val1.equals(expectedValue1)) {
                return false;
            }
            if (val2 != null && !val2.equals(expectedValue2)) {
                return false;
            }

            return true; // Beide null oder konsistent
        }
    }

    public static class NeighborConstraint implements Constraint {
        private Variable var1;
        private Object value1;
        private Variable var2;
        private Object value2;
        private List<List<Variable>> allHouseProperties;
        private List<Variable> scope;

        public NeighborConstraint(Variable var1, Object value1,
                Variable var2, Object value2,
                List<List<Variable>> allHouseProperties) {
            this.var1 = var1;
            this.value1 = value1;
            this.var2 = var2;
            this.value2 = value2;
            this.allHouseProperties = allHouseProperties;

            scope = new ArrayList<>();
            // Alle Variablen in den Scope aufnehmen für korrekte Propagation
            for (List<Variable> houseVars : allHouseProperties) {
                scope.addAll(houseVars);
            }
        }

        @Override
        public List<Variable> getScope() {
            return scope;
        }

        @Override
        public boolean isSatisfiedWith(Assignment assignment) {
            Integer house1 = findHouseForValue(assignment, var1, value1);
            Integer house2 = findHouseForValue(assignment, var2, value2);

            if (house1 == null || house2 == null) {
                return true; // Noch nicht bestimmbar
            }

            return Math.abs(house1 - house2) == 1; // Nachbarn
        }

        private Integer findHouseForValue(Assignment assignment, Variable varType, Object value) {
            for (int i = 0; i < allHouseProperties.size(); i++) {
                List<Variable> houseVars = allHouseProperties.get(i);
                // Finde die Variable des gesuchten Typs in diesem Haus
                for (Variable var : houseVars) {
                    if (var.getName().equals(varType.getName())) {
                        Object currentValue = assignment.getAssignment(var);
                        if (value.equals(currentValue)) {
                            return i; // Hausindex (0-basiert)
                        }
                    }
                }
            }
            return null;
        }
    }

    public static class LeftOfConstraint implements Constraint {
        private Variable leftVar;
        private Variable rightVar;
        private List<Variable> scope;

        public LeftOfConstraint(Variable leftVar, Variable rightVar, Variable positionVar) {
            this.leftVar = leftVar;
            this.rightVar = rightVar;
            this.scope = List.of(leftVar, rightVar, positionVar);
        }

        @Override
        public List<Variable> getScope() {
            return scope;
        }

        @Override
        public boolean isSatisfiedWith(Assignment assignment) {
            Object leftPos = assignment.getAssignment(leftVar);
            Object rightPos = assignment.getAssignment(rightVar);

            if (leftPos == null || rightPos == null) {
                return true;
            }

            int leftHouse = (int) leftPos;
            int rightHouse = (int) rightPos;

            return leftHouse == rightHouse - 1; // Direkt links daneben
        }
    }

    public static class AllDifferentConstraint implements Constraint {
        private List<Variable> variables;

        public AllDifferentConstraint(List<Variable> variables) {
            this.variables = new ArrayList<>(variables);
        }

        @Override
        public List<Variable> getScope() {
            return variables;
        }

        @Override
        public boolean isSatisfiedWith(Assignment assignment) {
            Set<Object> values = new HashSet<>();

            for (Variable var : variables) {
                Object value = assignment.getAssignment(var);
                if (value != null) {
                    if (!values.add(value)) {
                        return false; // Doppelter Wert gefunden
                    }
                }
            }
            return true;
        }
    }

    public static class ImplicationConstraint implements Constraint {
        private Variable conditionVar;
        private Object conditionValue;
        private Variable resultVar;
        private Object resultValue;
        private List<Variable> scope;

        public ImplicationConstraint(Variable conditionVar, Object conditionValue,
                Variable resultVar, Object resultValue) {
            this.conditionVar = conditionVar;
            this.conditionValue = conditionValue;
            this.resultVar = resultVar;
            this.resultValue = resultValue;
            this.scope = List.of(conditionVar, resultVar);
        }

        @Override
        public List<Variable> getScope() {
            return scope;
        }

        @Override
        public boolean isSatisfiedWith(Assignment assignment) {
            Object condVal = assignment.getAssignment(conditionVar);
            Object resVal = assignment.getAssignment(resultVar);

            // Wenn Bedingung nicht erfüllt ist, ist Constraint erfüllt
            if (condVal == null || !condVal.equals(conditionValue)) {
                return true;
            }

            // Wenn Bedingung erfüllt ist, muss Ergebnis stimmen
            return resVal == null || resVal.equals(resultValue);
        }
    }

    public class UnaryFixedValueConstraint implements Constraint {
        private Variable variable;
        private Object requiredValue;
        private List<Variable> scope;

        public UnaryFixedValueConstraint(Variable variable, Object requiredValue) {
            this.variable = variable;
            this.requiredValue = requiredValue;
            this.scope = List.of(variable);
        }

        @Override
        public List<Variable> getScope() {
            return scope;
        }

        @Override
        public boolean isSatisfiedWith(Assignment assignment) {
            Object value = assignment.getAssignment(variable);
            return value == null || value.equals(requiredValue);
        }
    }

}
