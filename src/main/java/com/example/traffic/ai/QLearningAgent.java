package com.example.traffic.ai;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * Agent de Q-Learning pour l'optimisation des feux tricolores.
 *
 * Actions possibles :
 *   0 = Feux Ouest/Est au VERT (Nord/Sud au ROUGE)
 *   1 = Feux Nord/Sud au VERT (Ouest/Est au ROUGE)
 *
 * L'agent apprend en temps réel quelle configuration de feux
 * minimise le temps d'attente des véhicules.
 */
public class QLearningAgent {

    // Table Q : état → [valeur action 0, valeur action 1]
    private final Map<String, double[]> qTable = new HashMap<>();

    // Hyperparamètres
    private double alpha = 0.1;     // Taux d'apprentissage
    private double gamma = 0.9;     // Facteur de discount (importance du futur)
    private double epsilon = 0.3;   // Taux d'exploration (30% au début)
    private double epsilonDecay = 0.999; // Décroissance de l'exploration
    private double epsilonMin = 0.05;    // Exploration minimale

    private final Random random = new Random();

    // Statistiques
    private double totalReward = 0.0;
    private int totalSteps = 0;

    // Dernière décision (pour le panneau IA)
    private String lastDecisionReason = "En attente...";
    private double lastConfidence = 0.0;
    private String lastAction = "KEEP";

    /**
     * Choisit une action avec la stratégie epsilon-greedy.
     * @param state l'état actuel du trafic
     * @return 0 (vert WE) ou 1 (vert NS)
     */
    public int chooseAction(TrafficState state) {
        double[] qValues = getQValues(state);

        // Exploration : action aléatoire
        if (random.nextDouble() < epsilon) {
            int action = random.nextInt(2);
            lastConfidence = 0.5;
            lastDecisionReason = "Exploration aléatoire (ε=" + String.format("%.0f", epsilon * 100) + "%)";
            lastAction = action == 0 ? "VERT E/O" : "VERT N/S";
            return action;
        }

        // Exploitation : meilleure action connue
        int bestAction = qValues[0] >= qValues[1] ? 0 : 1;
        double maxQ = Math.max(qValues[0], qValues[1]);
        double minQ = Math.min(qValues[0], qValues[1]);
        lastConfidence = maxQ == 0 && minQ == 0 ? 0.5 : Math.min(0.99, 0.6 + Math.abs(maxQ - minQ) * 0.1);

        if (bestAction == 0) {
            lastDecisionReason = String.format("E/O prioritaire (Q=%.2f vs %.2f). %d véh. E/O en attente.",
                    qValues[0], qValues[1], state.getWaitingWestEast());
            lastAction = "VERT E/O";
        } else {
            lastDecisionReason = String.format("N/S prioritaire (Q=%.2f vs %.2f). %d véh. N/S en attente.",
                    qValues[1], qValues[0], state.getWaitingNorthSouth());
            lastAction = "VERT N/S";
        }

        return bestAction;
    }

    /**
     * Met à jour la table Q avec la formule de Bellman :
     * Q(s,a) = Q(s,a) + α * [r + γ * max(Q(s',a')) - Q(s,a)]
     */
    public void learn(TrafficState state, int action, double reward, TrafficState nextState) {
        double[] qValues = getQValues(state);
        double[] nextQValues = getQValues(nextState);

        double maxNextQ = Math.max(nextQValues[0], nextQValues[1]);
        double oldQ = qValues[action];

        // Formule de Bellman
        qValues[action] = oldQ + alpha * (reward + gamma * maxNextQ - oldQ);

        // Décroissance de l'exploration
        if (epsilon > epsilonMin) {
            epsilon *= epsilonDecay;
        }

        // Statistiques
        totalReward += reward;
        totalSteps++;
    }

    /**
     * Récupère ou initialise les valeurs Q pour un état donné.
     */
    private double[] getQValues(TrafficState state) {
        return qTable.computeIfAbsent(state.toKey(), k -> new double[]{0.0, 0.0});
    }

    // ── Getters ──────────────────────────────────────────

    public double getTotalReward() { return totalReward; }
    public int getTotalSteps() { return totalSteps; }

    public double getAverageReward() {
        return totalSteps == 0 ? 0.0 : totalReward / totalSteps;
    }

    public double getEpsilon() { return epsilon; }
    public int getQTableSize() { return qTable.size(); }

    // ── Infos décision pour le panneau IA ──────────────
    public String getLastDecisionReason() { return lastDecisionReason; }
    public double getLastConfidence() { return lastConfidence; }
    public String getLastActionName() { return lastAction; }

    /** Réinitialise l'agent */
    public void reset() {
        qTable.clear();
        totalReward = 0.0;
        totalSteps = 0;
        epsilon = 0.3;
        lastDecisionReason = "En attente...";
        lastConfidence = 0.0;
        lastAction = "KEEP";
    }
}
