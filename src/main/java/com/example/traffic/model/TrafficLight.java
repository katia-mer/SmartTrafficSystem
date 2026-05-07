package com.example.traffic.model;

/**
 * Feu tricolore avec cycle français à 8 phases.
 * Phases : GREEN → YELLOW → RED → RED_YELLOW → (autre direction)
 */
public class TrafficLight {

    public enum State {
        RED, GREEN, YELLOW, RED_YELLOW
    }

    private State state;
    private double timer;
    private boolean aiControlled = false;

    // Cycle français 8 phases (partagé entre paires de feux)
    private int cyclePhase = 0;
    private static final int TOTAL_PHASES = 8;

    // Durées des phases (en secondes)
    private static final double GREEN_DURATION = 10.0;
    private static final double YELLOW_DURATION = 3.0;
    private static final double CLEARANCE_DURATION = 1.0;
    private static final double RED_YELLOW_DURATION = 2.0;

    public TrafficLight() {
        this.state = State.RED;
        this.timer = GREEN_DURATION;
    }

    /** Mise à jour automatique par timer (mode classique — cycle français) */
    public void update(double deltaTime) {
        if (aiControlled) return;

        timer -= deltaTime;
        if (timer <= 0) {
            nextPhase();
        }
    }

    /** Avance au cycle suivant (8 phases françaises) */
    private void nextPhase() {
        cyclePhase = (cyclePhase + 1) % TOTAL_PHASES;
        applyPhase();
    }

    /** Applique l'état correspondant à la phase courante */
    private void applyPhase() {
        switch (cyclePhase) {
            case 0: state = State.GREEN;      timer = GREEN_DURATION;      break;
            case 1: state = State.YELLOW;     timer = YELLOW_DURATION;     break;
            case 2: state = State.RED;        timer = CLEARANCE_DURATION;  break;
            case 3: state = State.RED;        timer = RED_YELLOW_DURATION; break;
            case 4: state = State.RED;        timer = GREEN_DURATION;      break;
            case 5: state = State.RED;        timer = YELLOW_DURATION;     break;
            case 6: state = State.RED;        timer = CLEARANCE_DURATION;  break;
            case 7: state = State.RED_YELLOW; timer = RED_YELLOW_DURATION; break;
        }
    }

    public void toggle() {
        state = (state == State.RED || state == State.RED_YELLOW) ? State.GREEN : State.RED;
    }

    /** Permet à l'IA ou au contrôleur de forcer l'état du feu */
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

    public boolean isYellow() {
        return state == State.YELLOW;
    }

    public boolean isRedYellow() {
        return state == State.RED_YELLOW;
    }

    public boolean isRed() {
        return state == State.RED;
    }

    /** Le véhicule peut-il passer ? (vert ou jaune proche) */
    public boolean canPass() {
        return state == State.GREEN || state == State.YELLOW;
    }

    public State getState() {
        return state;
    }

    public double getTimer() {
        return timer;
    }

    public void setTimer(double timer) {
        this.timer = timer;
    }

    public int getCyclePhase() {
        return cyclePhase;
    }

    public void setCyclePhase(int phase) {
        this.cyclePhase = phase;
        applyPhase();
    }
}