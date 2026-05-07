package com.example.traffic.model;

public class TrafficLight {

    public enum State {
        RED, GREEN
    }

    private State state;
    private double timer; // temps avant changement
    private boolean aiControlled = false;

    public TrafficLight() {
        this.state = State.RED;
        this.timer = 5.0; // secondes
    }

    /** Mise à jour automatique par timer (mode classique) */
    public void update(double deltaTime) {
        if (aiControlled) return; // L'IA contrôle directement

        timer -= deltaTime;
        if (timer <= 0) {
            toggle();
            timer = 5.0;
        }
    }

    public void toggle() {
        state = (state == State.RED) ? State.GREEN : State.RED;
    }

    /** Permet à l'IA de forcer l'état du feu */
    public void setState(State state) {
        this.state = state;
    }

    public void setAiControlled(boolean aiControlled) {
        this.aiControlled = aiControlled;
    }

    public boolean isAiControlled() {
        return aiControlled;
    }

    public boolean isGreen() {
        return state == State.GREEN;
    }

    public State getState() {
        return state;
    }
}