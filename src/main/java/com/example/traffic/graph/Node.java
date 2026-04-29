package com.example.traffic.graph;

/**
 * Représente un nœud du graphe routier (= une intersection).
 */
public class Node {

    private final String id;
    private final double x;
    private final double y;

    // ── Constructeur ──────────────────────────────────────
    public Node(String id, double x, double y) {
        this.id = id;
        this.x  = x;
        this.y  = y;
    }

    // ── Getters ───────────────────────────────────────────
    public String getId()  { return id; }
    public double getX()   { return x;  }
    public double getY()   { return y;  }

    // ── Distance euclidienne vers un autre nœud ───────────
    public double distanceTo(Node other) {
        double dx = this.x - other.x;
        double dy = this.y - other.y;
        return Math.sqrt(dx * dx + dy * dy);
    }

    @Override
    public String toString() {
        return "Node[" + id + " (" + x + "," + y + ")]";
    }
}