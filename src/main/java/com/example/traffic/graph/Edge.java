package com.example.traffic.graph;

/**
 * Représente une arête du graphe routier (= une route entre deux intersections).
 */
public class Edge {

    private final Node source;
    private final Node destination;
    private final double length;
    private final int    speedLimit; // km/h

    // ── Constructeur avec longueur manuelle ───────────────
    public Edge(Node source, Node destination, double length, int speedLimit) {
        this.source      = source;
        this.destination = destination;
        this.length      = length;
        this.speedLimit  = speedLimit;
    }

    // ── Constructeur qui calcule la longueur automatiquement
    public Edge(Node source, Node destination, int speedLimit) {
        this(source, destination, source.distanceTo(destination), speedLimit);
    }

    // ── Getters ───────────────────────────────────────────
    public Node   getSource()      { return source;      }
    public Node   getDestination() { return destination; }
    public double getLength()      { return length;      }
    public int    getSpeedLimit()  { return speedLimit;  }

    // ── Temps de parcours en secondes ─────────────────────
    public double getTravelTime() {
        double speedMs = speedLimit / 3.6; // km/h → m/s
        return length / speedMs;
    }

    @Override
    public String toString() {
        return "Edge[" + source.getId()
                + " → " + destination.getId()
                + ", " + String.format("%.1f", length) + "m"
                + " @" + speedLimit + "km/h]";
    }
}