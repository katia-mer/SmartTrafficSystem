package com.example.traffic.ai;

import com.example.traffic.model.Vehicle;
import com.example.traffic.simulation.SimulationEngine;

import java.util.List;

/**
 * Encode l'état du trafic en une clé unique pour la table Q.
 *
 * DISCRÉTISATION (State Binning) :
 * Au lieu d'utiliser le score exact d'attente (ce qui créerait des centaines
 * d'états), on regroupe en 3 catégories : FAIBLE, MOYEN, FORT.
 * Résultat : seulement 3x3 = 9 états possibles au lieu de 121.
 * L'IA converge ainsi quasi-instantanément.
 */
public class TrafficState {

    /**
     * Niveau de pression du trafic pour un axe donné.
     * - FAIBLE : peu de voitures, ou attente courte
     * - MOYEN  : accumulation modérée
     * - FORT   : embouteillage sérieux, priorité requise
     */
    public enum TrafficLevel {
        FAIBLE, MOYEN, FORT
    }

    private final TrafficLevel levelWestEast;
    private final TrafficLevel levelNorthSouth;

    public TrafficState(TrafficLevel levelWestEast, TrafficLevel levelNorthSouth) {
        this.levelWestEast = levelWestEast;
        this.levelNorthSouth = levelNorthSouth;
    }

    /**
     * Convertit un score brut de temps d'attente en niveau discrétisé.
     * Seuils calibrés pour la simulation :
     *   score < 15  → FAIBLE (1 ou 2 voitures venant d'arriver)
     *   score < 40  → MOYEN  (accumulation modérée)
     *   score >= 40 → FORT   (embouteillage sérieux)
     */
    private static TrafficLevel toLevel(double score) {
        if (score < 15.0) return TrafficLevel.FAIBLE;
        if (score < 40.0) return TrafficLevel.MOYEN;
        return TrafficLevel.FORT;
    }

    /**
     * Construit l'état à partir du moteur de simulation.
     * Somme les temps d'attente par axe, puis discrétise en TrafficLevel.
     */
    public static TrafficState fromSimulation(SimulationEngine engine) {
        double weWait = 0;
        double nsWait = 0;

        List<Vehicle> vehicles = engine.getVehicles();
        for (Vehicle v : vehicles) {
            if (v.isStopped() || v.getProgress() >= 0.8) {
                if (v.getCurrentEdge() == null) continue;

                String edgeId = v.getCurrentEdge().getSource().getId();
                // +1 garantit qu'un véhicule qui vient de s'arrêter compte immédiatement
                double waitScore = v.getWaitTime() + 1.0;

                if (edgeId.startsWith("W") || edgeId.startsWith("E")) {
                    weWait += waitScore;
                } else {
                    nsWait += waitScore;
                }
            }
        }

        return new TrafficState(toLevel(weWait), toLevel(nsWait));
    }

    /**
     * Clé unique pour la table Q.
     * Exemple : "FAIBLE_FORT" → seulement 9 combinaisons possibles.
     */
    public String toKey() {
        return levelWestEast.name() + "_" + levelNorthSouth.name();
    }

    /** Retourne le score numérique WE pour compatibilité avec les logs */
    public int getWaitingWestEast() {
        return levelWestEast.ordinal();
    }

    /** Retourne le score numérique NS pour compatibilité avec les logs */
    public int getWaitingNorthSouth() {
        return levelNorthSouth.ordinal();
    }

    public TrafficLevel getLevelWestEast() { return levelWestEast; }
    public TrafficLevel getLevelNorthSouth() { return levelNorthSouth; }

    @Override
    public String toString() {
        return "State[WE=" + levelWestEast + ", NS=" + levelNorthSouth + "]";
    }
}
