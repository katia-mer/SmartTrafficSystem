package com.example.traffic.ai;

import com.example.traffic.model.Vehicle;
import com.example.traffic.simulation.SimulationEngine;

import java.util.List;

/**
 * Calcule la récompense pour l'agent Q-Learning.
 * Récompense positive quand les voitures avancent,
 * négative quand elles sont bloquées.
 */
public class RewardCalculator {

    /**
     * Calcule la récompense instantanée.
     * @return valeur de récompense (plus c'est élevé, mieux c'est)
     */
    public static double calculate(SimulationEngine engine) {
        double reward = 0.0;
        List<Vehicle> vehicles = engine.getVehicles();

        for (Vehicle v : vehicles) {
            if (v.isStopped()) {
                // Pénalité croissante avec le temps d'attente pour éviter la famine
                double waitPenalty = 1.0 + (v.getWaitTime() * 0.5);
                reward -= waitPenalty; 
            } else {
                reward += 0.5; // bonus pour chaque voiture qui roule
            }
        }

        return reward;
    }
}
