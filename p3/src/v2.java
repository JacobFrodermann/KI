import java.util.Arrays;

class v2 {

    static int nodesEvaluated = 0;
    static int nodesPruned = 0;

    public static void main(String[] args) {
        Integer[] initialState = new Integer[]{0,0,0,0,0,0,0,0,0};
       
        /*
        nodesEvaluated = 0;
        nodesPruned = 0;
        Node root1 = new Node(initialState, 1, null);
        int value1 = minimax(root1, true);
        System.out.println("Bester Wert: " + value1);
        System.out.println("Knoten evaluiert: " + nodesEvaluated);
        */

        nodesEvaluated = 0;
        nodesPruned = 0;
        Node root2 = new Node(initialState, 1, null);
        int value2 = alphabeta(root2, Integer.MIN_VALUE, Integer.MAX_VALUE, true);
        System.out.println("Bester Wert: " + value2);
        System.out.println("Knoten evaluiert: " + nodesEvaluated);
        System.out.println("Knoten beschnitten: " + nodesPruned);
    }

    static int minimax(Node node, boolean maximizing) {
        nodesEvaluated++;
        
        if (node.isTerminal()) {
            return node.evaluate();
        }
        
        if (maximizing) {
            int maxVal = Integer.MIN_VALUE;
            for (Integer[] childState : node.generateAllMoves()) {
                Node child = new Node(childState, -node.player, node);
                int val = minimax(child, false);
                maxVal = Math.max(maxVal, val);
            }
            return maxVal;
        } else {
            int minVal = Integer.MAX_VALUE;
            for (Integer[] childState : node.generateAllMoves()) {
                Node child = new Node(childState, -node.player, node);
                int val = minimax(child, true);
                minVal = Math.min(minVal, val);
            }
            return minVal;
        }
    }

    static int alphabeta(Node node, int alpha, int beta, boolean maximizing) {
        nodesEvaluated++;
        
        if (node.isTerminal()) {
            return node.evaluate();
        }
        
        if (maximizing) {
            int maxVal = Integer.MIN_VALUE;
            for (Integer[] childState : node.generateAllMoves()) {
                Node child = new Node(childState, -node.player, node);
                int val = alphabeta(child, alpha, beta, false);
                maxVal = Math.max(maxVal, val);
                alpha = Math.max(alpha, val);
                
                // Beta Cutoff: Wenn maxVal >= beta, brauchen wir nicht weiter suchen
                if (beta <= alpha) {
                    nodesPruned++;
                    break;  // Pruning!
                }
            }
            return maxVal;
        } else {
            int minVal = Integer.MAX_VALUE;
            for (Integer[] childState : node.generateAllMoves()) {
                Node child = new Node(childState, -node.player, node);
                int val = alphabeta(child, alpha, beta, true);
                minVal = Math.min(minVal, val);
                beta = Math.min(beta, val);
                
                // Alpha Cutoff: minVal <= alpha
                if (beta <= alpha) {
                    nodesPruned++;
                    break;
                }
            }
            return minVal;
        }
    }
    
    static int alphaBetaOptimised(Node node, int player, int alpha, int beta) {
        nodesEvaluated++;
        
        if (node.isTerminal()) {
            return node.evaluate() * player;
        }
        
        int bestValue = Integer.MIN_VALUE;
        
        for (Integer[] childState : node.generateAllMoves()) {
            Node child = new Node(childState, -node.player, node);
            
            int value = -alphaBetaOptimised(child, -player, -beta, -alpha);
            
            bestValue = Math.max(bestValue, value);
            alpha = Math.max(alpha, value);
            
            if (alpha >= beta) {
                break;
            }
        }
        
        return bestValue;
    }

    static class Node {
        Integer[] state;
        int player;  // 1 oder -1
        Node parent;

        Node(Integer[] state, int player, Node parent) {
            this.state = state;
            this.player = player;
            this.parent = parent;
        }

        
        boolean isTerminal() {
            return hasWinner() || isFull();
        }

        Integer[][] generateAllMoves() {
            int emptyCount = 0;
            for (int i = 0; i < 9; i++) {
                if (state[i] == 0) emptyCount++;
            }
            
            Integer[][] moves = new Integer[emptyCount][9];
            int idx = 0;
            
            for (int i = 0; i < 9; i++) {
                if (state[i] == 0) {
                    moves[idx] = Arrays.copyOf(state, 9);
                    moves[idx][i] = player;
                    idx++;
                }
            }
            
            return moves;
        }

        // Prüft ob es einen Gewinner gibt
        boolean hasWinner() {
            // Zeilen und Spalten
            for (int i = 0; i < 3; i++) {
                if (horizontal(i)) 
                    return true;
                if (state[i] != 0 && state[i] == state[i+3] && state[i+3] == state[i+6]) 
                    return true;
            }
            
            // Diagonalen
            if (state[4] != 0) {
                if ((state[0] == state[4] && state[4] == state[8]) || 
                    (state[2] == state[4] && state[4] == state[6]))
                    return true;
            }
            
            return false;
        }

        // Prüft ob das Brett voll ist
        boolean isFull() {
            for (int i = 0; i < 9; i++) {
                if (state[i] == 0) return false;
            }
            return true;
        }

        // Bewertet den aktuellen Zustand
        int evaluate() {
            // Zeilen
            for (int i = 0; i < 3; i++) {
                if (horizontal(i)) 
                    return state[i*3];
            }
            
            // Spalten
            for (int i = 0; i < 3; i++) {
                if (state[i] != 0 && state[i] == state[i+3] && state[i+3] == state[i+6]) 
                    return state[i];
            }
            
            // Diagonalen
            if (state[4] != 0) {
                if (state[0] == state[4] && state[4] == state[8]) return state[4];
                if (state[2] == state[4] && state[4] == state[6]) return state[4];
            }
            
            // Unentschieden
            return 0;
        }

        boolean horizontal(int i) {
            return state[i*3] != 0 && state[i*3] == state[i*3+1] && state[i*3+1] == state[i*3+2];            
        }
    }
}