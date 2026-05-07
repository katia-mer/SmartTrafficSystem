package com.example.traffic.ai;

import com.example.traffic.model.Vehicle;
import com.example.traffic.simulation.SimulationEngine;

import java.util.List;

/**
 * Encode l'état du trafic en une clé unique pour la table Q.
 * L'état représente le nombre de voitures en attente dans chaque direction.
 */
public class TrafficState {

    private final int waitingWestEast;
    private final int waitingNorthSouth;

    public TrafficState(int waitingWestEast, int waitingNorthSouth) {
        this.waitingWestEast = waitingWestEast;
        this.waitingNorthSouth = waitingNorthSouth;
    }

    /**
     * Construit l'état à partir du moteur de simulation.
     * Compte les véhicules arrêtés dans chaque axe.
     */
    public static TrafficState fromSimulation(SimulationEngine engine) {
        int we = 0;
        int ns = 0;

        List<Vehicle> vehicles = engine.getVehicles();
        for (Vehicle v : vehicles) {
            if (v.isStopped() || v.getProgress() >= 0.8) {
                if (v.getCurrentEdge() == null) continue;

                String edgeId = v.getCurrentEdge().getSource().getId();
                if (edgeId.startsWith("W") || edgeId.startsWith("E")) {
                    we++;
                } else {
                    ns++;
                }
            }
        }

        return new TrafficState(we, ns);
    }

    /** Clé unique pour la table Q */
    public String toKey() {
        return waitingWestEast + "_" + waitingNorthSouth;
    }

    public int getWaitingWestEast() {
        return waitingWestEast;
    }

    public int getWaitingNorthSouth() {
        return waitingNorthSouth;
    }

    @Override
    public String toString() {
        return "State[WE=" + waitingWestEast + ", NS=" + waitingNorthSouth + "]";
    }
}
