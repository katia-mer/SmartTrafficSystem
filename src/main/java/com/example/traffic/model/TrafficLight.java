package com.example.traffic.model;

public class TrafficLight {

    public enum State {
        RED, GREEN
    }

    private State state;
    private double timer; // temps avant changement

    public TrafficLight() {
        this.state = State.RED;
        this.timer = 5.0; // secondes
    }

    public void update(double deltaTime) {
        timer -= deltaTime;
        if (timer <= 0) {
            toggle();
            timer = 5.0;
        }
    }

    public void toggle() {
        state = (state == State.RED) ? State.GREEN : State.RED;
    }

    public boolean isGreen() {
        return state == State.GREEN;
    }

    public State getState() {
        return state;
    }
}