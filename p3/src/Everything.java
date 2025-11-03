import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.OptionalInt;
import java.util.stream.IntStream;

public class Everything {

    public static void main(String[] args) {
        Node root = new Node(new Integer[]{0,0,0,0,0,0,0,0,0}, 1, null);

        List<LinkedList<Everything.Node>> Tree = new LinkedList<LinkedList<Node>>();

        Tree.add(new LinkedList<Node>());
        Tree.get(0).add(root);

        int depth = 0;

        while (true) {
            List<Node> current  = Tree.get(Tree.size()-1);
            LinkedList<Node> next = new LinkedList<>();
            current.forEach((Node n) -> {
                next.addAll(n.expand());
            });

            if (next.size() == 0) {
                break;
            }
            Tree.add(next);
            depth ++;
            System.out.println("depth " + depth);
        }

        ListIterator<LinkedList<Node>> li = Tree.listIterator(Tree.size());

        while (li.hasPrevious()) {
            li.previous().forEach(Node::calcValue);
        }


        int[] count = {0};
        Tree.forEach((List<Node> l) -> l.forEach((Node n) -> {count[0]++;}));
    
        System.out.println(count[0] + " Nodes");
    }

    static class Node {
        Integer[] State = new Integer[]{0,0,0,0,0,0,0,0,0};

        int toMove = 0;

        int value = 0;

        Node parent;

        Collection<Node> children;

        boolean evaluated = false;

        Node(Integer[] State, int toMove, Node parent) {
            this.State = State;
            this.toMove = toMove;
            this.parent = parent;
        }

        Collection<Node> expand() {
            if (hasWinner()) {
                evaluate();
                return new LinkedList<>();
            }

            LinkedList<Node> children = new LinkedList<>();

            for (int i = 0;i < State.length; i ++) {
                if (State[i] == 0) {
                    Integer[] newState = Arrays.copyOf(State, State.length);
                    newState[i] = toMove;
                    children.add(new Node(newState, toMove * -1, this));
                }
            }

            this.children = children;

            if (children.size() == 0) {
                evaluate();
            }

            return children;
        }
        
        boolean hasWinner() {
            boolean isTerm = false;
            
            for (int i = 0; i < 3; i++) {
                isTerm = isTerm || (State[i*3] != 0 && State[i*3+0] == State[i*3+1] && State[i*3+1] == State[i*3+2]);
                
                isTerm = isTerm || (State[i] != 0 && State[i+0] == State[i+3] && State[i+3] == State[i+6]);
                if (isTerm) return isTerm;
            }
            
            return State[4] != 0 && ((State[0] == State[4] && State[4] == State[8]) || (State[2] == State[4] && State[4] == State[6]));
        }

        int evaluate()  {
            evaluated = true;
            
            for (int i = 0; i < 3; i++) {
                if (State[i*3] != 0 && State[i*3+0] == State[i*3+1] && State[i*3+1] == State[i*3+2]) {
                    value = State[i*3];
                    return value;
                }
                
                if (State[i] != 0 && State[i+0] == State[i+3] && State[i+3] == State[i+6]) {
                    value = State[i];
                    return value;
                }
            }
            
            if (State[4] != 0 && ((State[0] == State[4] && State[4] == State[8]) || (State[2] == State[4] && State[4] == State[6]))) value = State[4];
            return value;
        }

        void calcValue() {
            boolean max = toMove == 1;

            IntStream values = children.stream().mapToInt(Node::getValue);
            OptionalInt opt = max ? values.max() : values.min();
            value = opt.isPresent() ? opt.getAsInt() : 0;
        }

        int getValue() {
            return value;
        }
    }
}
