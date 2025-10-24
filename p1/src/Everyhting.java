import java.util.*;
import java.util.function.Function;

public class Everyhting {
    public static void main(String[] args) {
        new Everyhting();
    }

        List<Node> cities = new ArrayList<>();

        Node Frankfurt = new Node("Frankfurt");
        Node Mannheim = new Node("Mannheim");
        Node Karlsruhe = new Node("Karlsruhe");
        Node Augsburg = new Node("Augsburg");
        Node Muenchen = new Node("München");
        Node Nuernberg = new Node("Nürnberg");
        Node Erfurt = new Node("Erfurt");
        Node Wuerzburg = new Node("Würzburg");
        Node Stuttgart = new Node("Stuttgart");
        Node Kassel = new Node("Kassel");

        Map<Node, Integer> costs = new HashMap<>();

        int BSteps = 0;

    Everyhting() {
        cities.add(Mannheim);
        cities.add(Karlsruhe);
        cities.add(Frankfurt);
        cities.add(Augsburg);
        cities.add(Muenchen);
        cities.add(Nuernberg);
        cities.add(Erfurt);
        cities.add(Wuerzburg);
        cities.add(Stuttgart);
        cities.add(Kassel);

        System.out.println("building graph");

        link(Frankfurt, Mannheim, 85);
        link(Frankfurt, Kassel, 173);
        link(Frankfurt, Wuerzburg, 217);

        link(Mannheim, Karlsruhe, 80);

        link(Karlsruhe, Augsburg, 250);

        link(Augsburg, Muenchen, 84);

        link(Wuerzburg, Erfurt, 186);
        link(Wuerzburg, Nuernberg, 103);

        link(Stuttgart, Nuernberg, 183);

        link(Kassel, Muenchen, 502);

        link(Nuernberg, Muenchen, 10);


        Node from = Wuerzburg;
        Node target = Muenchen;


        costs.put(Augsburg, 0);
        costs.put(Erfurt, 400);
        costs.put(Frankfurt, 100);
        costs.put(Karlsruhe, 10);
        costs.put(Kassel, 460);
        costs.put(Mannheim, 200);
        costs.put(Muenchen,0);
        costs.put(Nuernberg, 537);
        costs.put(Stuttgart,300);
        costs.put(Wuerzburg,170);

        System.out.println("\n\nTiefensuche");
        Tiefensuche(from, target, new ArrayList<>());

        System.out.println("\n\nBreitensuche");
        BreitenSuche(from, target);

        System.out.println("\n\nSternsuche");
        // heuristic spart tatsächlich ein Schritt zu (Node) -> 0
        SternSuche(from, target, this::fromCosts);
    }

    boolean Tiefensuche(Node from, Node to, List<Node> visited) {
        System.out.println("visiting " + from.name);
        BSteps ++;
        if (from == to) {
            System.out.println("found");
            System.out.println("in " + BSteps + " steps");
            return true;
        }

        visited.add(from);
        for (Vertex r : from.routes) {
            Node visit = r.A == from ? r.B : r.A;
            if (visited.contains(visit)) continue;

            if (Tiefensuche(visit, to, visited)) {
                return true;
            }
        }
        return  false;
    }

    boolean BreitenSuche(Node from, Node to) {
        Queue<Node> toVisit = new LinkedList<>();
        List<Node> visited = new ArrayList<>();

        toVisit.add(from);

        int steps = 0;

        boolean found = false;

        while (true) {
            steps ++;
            if (toVisit.isEmpty()) {
                System.out.println("Not found");
                break;
            }

            Node v = toVisit.remove();
            visited.add(v);

            System.out.println("visiting " + v.name);
            if (v == to) {
                System.out.println("found");
                System.out.println("in " + steps + " steps");
                found = true;
                break;
            }

                for (Vertex r : v.routes) {
                    Node visit = r.A == v ? r.B : r.A;
                    if (visited.contains(visit) || toVisit.contains(visit)) {
                        //System.out.println(visit.name + " was / is goign to be visited");
                        continue;
                    }

                    System.out.println("adding " + visit.name + " to Queue");
                    toVisit.add(visit);
                }
        }
        return found;
    }

    boolean SternSuche(Node from, Node to, Function<Node, Integer> h) {
        PriorityQueue<Entry> open = new PriorityQueue<>((Entry a, Entry b) -> a.weight - b.weight);
        Map<Node, Integer> costTo = new HashMap<>();
        Map<Node, Node> parrentOf = new HashMap<>();
        List<Node> closed = new ArrayList<>();

        int steps = 0;

        costTo.put(from, 0);

        open.add(new Entry(from,0));

        while (true) {
            if (open.isEmpty()) return false;

            Node current = open.remove().n;

            steps ++;

            if (current == to) {
                System.out.println("found");
                System.out.println("in " + steps + " steps");
                break;
            }

            closed.add(current);

            for (Vertex rel : current.routes) {
                Node toVisit = rel.A == current ? rel.B : rel.A;

                if (closed.contains(toVisit)) continue;

                int costToVisit = costTo.get(current) + rel.dist;

                Integer knownCostToVisit = costTo.get(toVisit);

                if (knownCostToVisit == null) {
                    knownCostToVisit = 1<<30;
                }

                boolean nbrOpen = contains(open, toVisit);

                if (!nbrOpen || costToVisit < knownCostToVisit) {
                    parrentOf.put(toVisit, current);
                    costTo.put(toVisit, costToVisit);

                    int estTotalCost = costToVisit + h.apply(toVisit);

                    if (nbrOpen) {
                        setWeight(open, toVisit, estTotalCost);
                    } else {
                        open.add(new Entry(toVisit, estTotalCost));
                    }

                }
            }
        }
        Node[] path = buildPath(parrentOf, to);
        print(path);
        return true;
    }

    void print(Node[] p) {
        for (int i = 0; i < p.length; i ++) {
            System.out.print(p[p.length-i-1].name);
            System.out.print("->");
        }
        System.out.println("found");
    }

    Node[] buildPath(Map<Node, Node> parentOf, Node to) {
        ArrayList<Node> p = new ArrayList<>();

        Node current = to;
        while (current != null) {
            if (p.contains(current)) {
                throw new IllegalStateException("cant loop");
            }
            p.add(current);

            current = parentOf.get(current);
        }

        return p.toArray(new Node[]{});
    }

    boolean contains(PriorityQueue<Entry> q, Node n) {
        for (Entry e : q) {
            if (e.n == n) {
                return  true;
            }
        }
        return  false;
    }

    void link(Node a, Node b, int dist) {
        Vertex route = new Vertex(a, b, dist);

        System.out.println(a.name + " -> " + b.name);


        a.routes.add(route);
        b.routes.add(route);
    }

    void setWeight(PriorityQueue<Entry> q, Node n, int newWeight) {
        Entry entryInQueue = null;
        for (Entry e : q) {
            if (e.n == n) {
                entryInQueue = e;
            }
        }

        if (entryInQueue == null) {
            throw new IllegalStateException();
        }

        q.remove(entryInQueue);
        q.add(new Entry(n, newWeight));
    }

    class Entry {
        Node n;
        Integer weight;
        Entry(Node n, Integer w) {
            this.n = n;
            weight = w;
        }
    }

    class Node {
        String name;
        int waeh;
        List<Vertex> routes = new ArrayList<>();

        Node(String name) {
            this.name = name;
        }
    }

    class Vertex {
        Node A, B;
        int dist;

        Vertex(Node A, Node B, int dist) {
            this.A = A;
            this.B = B;
            this.dist = dist;
        }
    }

    int fromCosts(Node c) {
        return costs.get(c);
    }
}
