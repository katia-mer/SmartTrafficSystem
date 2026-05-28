package com.example.traffic.ai;

import com.example.traffic.model.Vehicle;
import com.example.traffic.simulation.SimulationEngine;

import java.util.Deque;
import java.util.LinkedList;
import java.util.List;

/**
 * Encode l'état du trafic avec AMÉLIORATIONS:
 * 
 * 1. DISCRÉTISATION AMÉLIORÉE (5 niveaux au lieu de 3):
 *    - TRÈS_FAIBLE, FAIBLE, MOYEN, FORT, TRÈS_FORT
 *    Seuils adaptatifs basés sur statistiques
 * 
 * 2. TEMPORAL FEATURES (Mémoire temporelle):
 *    - Stocke l'historique des 3 derniers états
 *    - Détecte les TENDANCES (croissant/décroissant/stable)
 *    - Permet à l'IA d'ANTICIPER plutôt que juste réagir
 */
public class TrafficState {

    /**
     * 5 niveaux de pression du trafic (plus granulaire = meilleures décisions).
     * - TRÈS_FAIBLE : quasi vide
     * - FAIBLE      : peu de véhicules
     * - MOYEN       : normal
     * - FORT        : congestion
     * - TRÈS_FORT   : embouteillage sérieux
     */
    public enum TrafficLevel {
        TRÈS_FAIBLE, FAIBLE, MOYEN, FORT, TRÈS_FORT
    }

    /**
     * Tendance temporelle : permet à l'IA d'anticiper.
     * - DÉCROISSANT : trafic diminue → peut garder vert plus longtemps
     * - STABLE      : pas de changement → comportement standard
     * - CROISSANT   : trafic augmente → anticiper changement de feu
     */
    public enum Trend {
        DÉCROISSANT, STABLE, CROISSANT
    }

    private final TrafficLevel levelWestEast;
    private final TrafficLevel levelNorthSouth;
    private final Trend trendWestEast;
    private final Trend trendNorthSouth;

    // Historique pour les tendances
    private static final int HISTORY_SIZE = 3;
    private static final Deque<TrafficLevel> historyWE = new LinkedList<>();
    private static final Deque<TrafficLevel> historyNS = new LinkedList<>();

    public static void resetHistory() {
        historyWE.clear();
        historyNS.clear();
    }

    public TrafficState(TrafficLevel levelWestEast, TrafficLevel levelNorthSouth, 
                        Trend trendWestEast, Trend trendNorthSouth) {
        this.levelWestEast = levelWestEast;
        this.levelNorthSouth = levelNorthSouth;
        this.trendWestEast = trendWestEast;
        this.trendNorthSouth = trendNorthSouth;
    }

    /**
     * Convertit un score brut en 5 niveaux (granularité améliorée).
     * Seuils calibrés:
     *   score < 5   → TRÈS_FAIBLE
     *   score < 15  → FAIBLE
     *   score < 30  → MOYEN
     *   score < 50  → FORT
     *   score >= 50 → TRÈS_FORT
     */
    private static TrafficLevel toLevel(double score) {
        if (score < 5.0)   return TrafficLevel.TRÈS_FAIBLE;
        if (score < 15.0)  return TrafficLevel.FAIBLE;
        if (score < 30.0)  return TrafficLevel.MOYEN;
        if (score < 50.0)  return TrafficLevel.FORT;
        return TrafficLevel.TRÈS_FORT;
    }

    /**
     * Calcule la tendance en comparant les 3 derniers états.
     * @param history l'historique de niveaux
     * @return DÉCROISSANT, STABLE, ou CROISSANT
     */
    private static Trend calculateTrend(Deque<TrafficLevel> history) {
        if (history.size() < 2) {
            return Trend.STABLE;
        }
        
        int current = history.peek().ordinal();
        int previous = history.stream().skip(1).findFirst().orElse(TrafficLevel.MOYEN).ordinal();
        
        if (current > previous + 1) {
            return Trend.CROISSANT;
        } else if (current < previous - 1) {
            return Trend.DÉCROISSANT;
        }
        return Trend.STABLE;
    }

    /**
     * Construit l'état amélioré avec TEMPORAL FEATURES.
     * Somme les temps d'attente par axe, discrétise, et détecte tendances.
     */
    public static TrafficState fromSimulation(SimulationEngine engine) {
        double weWait = 0;
        double nsWait = 0;

        List<Vehicle> vehicles = engine.getVehicles();
        for (Vehicle v : vehicles) {
            if (v.isStopped() || v.getProgress() >= 0.8) {
                if (v.getCurrentEdge() == null) continue;

                String edgeId = v.getCurrentEdge().getSource().getId();
                double waitScore = v.getWaitTime() + 1.0;

                if (edgeId.startsWith("W") || edgeId.startsWith("E")) {
                    weWait += waitScore;
                } else {
                    nsWait += waitScore;
                }
            }
        }

        TrafficLevel levelWE = toLevel(weWait);
        TrafficLevel levelNS = toLevel(nsWait);

        // Mettre à jour l'historique
        historyWE.addFirst(levelWE);
        if (historyWE.size() > HISTORY_SIZE) {
            historyWE.removeLast();
        }
        
        historyNS.addFirst(levelNS);
        if (historyNS.size() > HISTORY_SIZE) {
            historyNS.removeLast();
        }

        // Calculer les tendances
        Trend trendWE = calculateTrend(historyWE);
        Trend trendNS = calculateTrend(historyNS);

        return new TrafficState(levelWE, levelNS, trendWE, trendNS);
    }

    /**
     * Clé unique pour la table Q (incluant tendances).
     * Exemple: "MOYEN_CROISSANT__FORT_STABLE"
     * = 5 niveaux × 3 tendances × 5 niveaux × 3 tendances = 225 états (vs 9 avant)
     * Plus granulaire = apprentissage plus fin, mais aussi plus d'exploration
     */
    public String toKey() {
        return levelWestEast.name() + "_" + trendWestEast.name() + "__" +
               levelNorthSouth.name() + "_" + trendNorthSouth.name();
    }

    // ── Getters ──────────────────────────────────────────
    public TrafficLevel getLevelWestEast() { return levelWestEast; }
    public TrafficLevel getLevelNorthSouth() { return levelNorthSouth; }
    public Trend getTrendWestEast() { return trendWestEast; }
    public Trend getTrendNorthSouth() { return trendNorthSouth; }

    /** Retourne le score numérique WE pour compatibilité */
    public int getWaitingWestEast() {
        return levelWestEast.ordinal();
    }

    /** Retourne le score numérique NS pour compatibilité */
    public int getWaitingNorthSouth() {
        return levelNorthSouth.ordinal();
    }

    @Override
    public String toString() {
        return "State[WE=" + levelWestEast + " (" + trendWestEast + ")" +
               ", NS=" + levelNorthSouth + " (" + trendNorthSouth + ")]";
    }
}
