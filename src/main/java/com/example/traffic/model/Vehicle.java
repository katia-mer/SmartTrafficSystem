package com.example.traffic.model;

import com.example.traffic.graph.Edge;
import com.example.traffic.graph.Node;

/**
 * Représente un véhicule dans le système de trafic.
 * Supporte les véhicules normaux et d'urgence.
 */
public class Vehicle {

    // ── Identité ──────────────────────────────────────────
    private final String id;

    // ── Vitesse (m/s) ─────────────────────────────────────
    private double speed;
    private final double baseSpeed;
    private double speedModifier = 1.0; // Pour la pluie, etc.

    // ── Position ──────────────────────────────────────────
    private Node currentNode;
    private Edge currentEdge;
    private Edge initialEdge;
    private double progress; // entre 0 et 1

    // ── État ──────────────────────────────────────────────
    private boolean stopped;
    private int preferredRouteIndex = 0;

    // ── Urgence ──────────────────────────────────────────
    private boolean emergency;
    private String emergencyType; // "ambulance", "fire", "police", "rescue"

    // ── Métriques ────────────────────────────────────────
    private double waitTime = 0.0;
    private boolean markedForRemoval = false;

    // ── Constructeur normal ──────────────────────────────
    public Vehicle(String id, Node startNode, double speed) {
        this.id = id;
        this.currentNode = startNode;
        this.speed = speed;
        this.baseSpeed = speed;
        this.progress = 0.0;
        this.stopped = false;
        this.currentEdge = null;
        this.emergency = false;
    }

    // ── Constructeur véhicule d'urgence ──────────────────
    public Vehicle(String id, Node startNode, double speed, String emergencyType) {
        this(id, startNode, speed * 1.5); // 50% plus rapide
        this.emergency = true;
        this.emergencyType = emergencyType;
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

    // ── Vitesse effective ─────────────────────────────────
    public double getEffectiveSpeed() {
        return speed * speedModifier;
    }

    // ── Getters ───────────────────────────────────────────
    public String getId() { return id; }
    public double getSpeed() { return speed; }
    public double getBaseSpeed() { return baseSpeed; }
    public Node getCurrentNode() { return currentNode; }
    public Edge getCurrentEdge() { return currentEdge; }
    public Edge getInitialEdge() { return initialEdge; }
    public double getProgress() { return progress; }
    public boolean isStopped() { return stopped; }
    public boolean isEmergency() { return emergency; }
    public String getEmergencyType() { return emergencyType; }
    public double getWaitTime() { return waitTime; }
    public boolean isMarkedForRemoval() { return markedForRemoval; }
    public double getSpeedModifier() { return speedModifier; }

    // ── Setters ───────────────────────────────────────────
    public void setCurrentNode(Node currentNode) { this.currentNode = currentNode; }

    public void setCurrentEdge(Edge currentEdge) {
        this.currentEdge = currentEdge;
        if (this.initialEdge == null) {
            this.initialEdge = currentEdge;
        }
    }

    public void setInitialEdge(Edge initialEdge) { this.initialEdge = initialEdge; }
    public int getPreferredRouteIndex() { return preferredRouteIndex; }
    public void setPreferredRouteIndex(int preferredRouteIndex) { this.preferredRouteIndex = preferredRouteIndex; }
    public void setProgress(double progress) { this.progress = progress; }
    public void setStopped(boolean stopped) { this.stopped = stopped; }
    public void setSpeed(double speed) { this.speed = speed; }
    public void setSpeedModifier(double modifier) { this.speedModifier = modifier; }
    public void setMarkedForRemoval(boolean marked) { this.markedForRemoval = marked; }

    public void addWaitTime(double dt) { this.waitTime += dt; }

    public void resetWaitTime() { this.waitTime = 0.0; }

    public void resetProgress() { this.progress = 0.0; }

    @Override
    public String toString() {
        String prefix = emergency ? "🚨" + emergencyType : "Vehicle";
        return prefix + "[" + id +
                " x=" + String.format("%.2f", getX()) +
                " y=" + String.format("%.2f", getY()) +
                " progress=" + String.format("%.2f", progress) +
                " stopped=" + stopped +
                "]";
    }
}