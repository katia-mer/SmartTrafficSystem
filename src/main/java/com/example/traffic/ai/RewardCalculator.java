package com.example.traffic.ai;

import com.example.traffic.model.Vehicle;
import com.example.traffic.simulation.SimulationEngine;

import java.util.List;

/**
 * Calcule la récompense pour l'agent Q-Learning.
 * AMÉLIORATIONS:
 * - Récompense positive quand les voitures avancent
 * - Pénalité croissante pour éviter la famine (attente excessive)
 * - Bonus/pénalité pour équité entre les axes
 * - Bonus spécial pour véhicules d'urgence
 * - Débit global (throughput) récompensé
 */
public class RewardCalculator {

    // Pondérations (peut être ajustées selon les priorités)
    private static final double VEHICLE_MOVING_BONUS = 0.5;
    private static final double VEHICLE_STOPPED_PENALTY = 1.0;
    private static final double WAIT_TIME_MULTIPLIER = 0.5; // Croissance avec attente
    private static final double FAIRNESS_BONUS = 1.5; // Récompense pour équité
    private static final double THROUGHPUT_BONUS = 0.8; // Véhicules complétés
    private static final double EMERGENCY_PRIORITY_BONUS = 5.0; // Gros bonus si urgence peut passer

    /**
     * Calcule la récompense instantanée améliorée.
     * Considère: véhicules en mouvement, équité entre axes, débit, et urgences
     * @return valeur de récompense (plus c'est élevé, mieux c'est)
     */
    public static double calculate(SimulationEngine engine) {
        double reward = 0.0;
        List<Vehicle> vehicles = engine.getVehicles();
        
        if (vehicles.isEmpty()) {
            return reward;
        }

        // 1. Récompense pour véhicules en mouvement / pénalité pour arrêts
        double movingCount = 0;
        double stoppedCount = 0;
        double emergencyStoppedCount = 0;
        double maxWaitTime = 0.0;
        
        double totalWaitNS = 0.0;
        double totalWaitEW = 0.0;
        int countNS = 0;
        int countEW = 0;

        for (Vehicle v : vehicles) {
            if (v.isEmergency() && v.isStopped()) {
                emergencyStoppedCount++;
                // Pénalité TRÈS élevée si une ambulance/pompiers est bloquée
                reward -= 10.0;
            } else if (v.isStopped()) {
                stoppedCount++;
                // Pénalité croissante avec le temps d'attente
                double waitPenalty = VEHICLE_STOPPED_PENALTY + (v.getWaitTime() * WAIT_TIME_MULTIPLIER);
                reward -= waitPenalty;
                maxWaitTime = Math.max(maxWaitTime, v.getWaitTime());
            } else {
                movingCount++;
                reward += VEHICLE_MOVING_BONUS;
            }
            
            // Collecte des données par axe pour équité
            if (v.getCurrentEdge() != null) {
                String edgeId = v.getCurrentEdge().getSource().getId();
                if (edgeId.startsWith("W") || edgeId.startsWith("E")) {
                    totalWaitEW += v.getWaitTime();
                    countEW++;
                } else {
                    totalWaitNS += v.getWaitTime();
                    countNS++;
                }
            }
        }

        // 2. BONUS ÉQUITÉ: Récompenser quand les deux axes ont une attente similaire
        //    Cela évite de laisser un axe accumul les retards
        if (countNS > 0 && countEW > 0) {
            double avgWaitNS = totalWaitNS / countNS;
            double avgWaitEW = totalWaitEW / countEW;
            double fairnessDiff = Math.abs(avgWaitNS - avgWaitEW);
            
            // Si la différence est faible, c'est bon (équitable)
            // Si elle est grande, on pénalise
            if (fairnessDiff < 5.0) {
                reward += FAIRNESS_BONUS;
            } else {
                reward -= (fairnessDiff * 0.1); // pénalité proportionnelle à l'inéquité
            }
        }

        // 3. Prévention de la famine: Pénalité extrême si un axe attend trop longtemps
        if (maxWaitTime > 60.0) {
            reward -= (maxWaitTime - 60.0) * 0.5; // pénalité croissante
        }

        // 4. BONUS pour débit (throughput): Récompenser les véhicules qui complètent leur trajet
        //    Cette métrique sera développée dans SimulationEngine si nécessaire
        int vehiclesCompleted = engine.getVehiclesCompleted();
        reward += vehiclesCompleted * THROUGHPUT_BONUS;

        return reward;
    }

    /**
     * Calcule une récompense spécifique pour les véhicules d'urgence.
     * @param emergencyVehicle le véhicule d'urgence
     * @param isStopped s'il est actuellement arrêté
     * @return bonus/pénalité pour urgence
     */
    public static double calculateEmergencyReward(Vehicle emergencyVehicle, boolean isStopped) {
        if (!emergencyVehicle.isEmergency()) {
            return 0.0;
        }
        
        if (isStopped) {
            // Pénalité très sévère si urgence bloquée
            return -10.0 - (emergencyVehicle.getWaitTime() * 2.0);
        } else {
            // Bonus important si urgence roule
            return EMERGENCY_PRIORITY_BONUS;
        }
    }
}
