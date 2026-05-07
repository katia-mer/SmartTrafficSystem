package com.example.traffic.model;

import com.example.traffic.graph.Edge;
import com.example.traffic.graph.Node;

/**
 * Représente un véhicule dans le système de trafic.
 */
public class Vehicle {

    // ── Identité ──────────────────────────────────────────
    private final String id;

    // ── Vitesse (m/s) ─────────────────────────────────────
    private final double speed;

    // ── Position ──────────────────────────────────────────
    private Node currentNode;
    private Edge currentEdge;
    private Edge initialEdge;
    private double progress; // entre 0 et 1

    // ── État ──────────────────────────────────────────────
    private boolean stopped;
    private int preferredRouteIndex = 0; // 0 = tout droit, 1 = tourner à droite, etc.

    // ── Constructeur ──────────────────────────────────────
    public Vehicle(String id, Node startNode, double speed) {
        this.id = id;
        this.currentNode = startNode;
        this.speed = speed;
        this.progress = 0.0;
        this.stopped = false;
        this.currentEdge = null;
    }

    // ── Position X (interpolation) ────────────────────────
    public double getX() {
        if (currentEdge == null) return currentNode.getX();

        double x1 = currentEdge.getSource().getX();
        double x2 = currentEdge.getDestination().getX();

        return x1 + (x2 - x1) * progress;
    }

    // ── Position Y (interpolation) ────────────────────────
    public double getY() {
        if (currentEdge == null) return currentNode.getY();

        double y1 = currentEdge.getSource().getY();
        double y2 = currentEdge.getDestination().getY();

        return y1 + (y2 - y1) * progress;
    }

    // ── Getters ───────────────────────────────────────────
    public String getId() {
        return id;
    }

    public double getSpeed() {
        return speed;
    }

    public Node getCurrentNode() {
        return currentNode;
    }

    public Edge getCurrentEdge() {
        return currentEdge;
    }

    public Edge getInitialEdge() {
        return initialEdge;
    }

    public double getProgress() {
        return progress;
    }

    public boolean isStopped() {
        return stopped;
    }

    // ── Setters ───────────────────────────────────────────
    public void setCurrentNode(Node currentNode) {
        this.currentNode = currentNode;
    }

    public void setCurrentEdge(Edge currentEdge) {
        this.currentEdge = currentEdge;
        if (this.initialEdge == null) {
            this.initialEdge = currentEdge;
        }
    }

    public void setInitialEdge(Edge initialEdge) {
        this.initialEdge = initialEdge;
    }

    public int getPreferredRouteIndex() {
        return preferredRouteIndex;
    }

    public void setPreferredRouteIndex(int preferredRouteIndex) {
        this.preferredRouteIndex = preferredRouteIndex;
    }

    public void setProgress(double progress) {
        this.progress = progress;
    }

    public void setStopped(boolean stopped) {
        this.stopped = stopped;
    }

    // ── Reset position (utile pour simulation) ────────────
    public void resetProgress() {
        this.progress = 0.0;
    }

    @Override
    public String toString() {
        return "Vehicle[" + id +
                " x=" + String.format("%.2f", getX()) +
                " y=" + String.format("%.2f", getY()) +
                " progress=" + String.format("%.2f", progress) +
                " stopped=" + stopped +
                "]";
    }
}