package com.example.traffic.simulation;

import com.example.traffic.ai.QLearningAgent;
import com.example.traffic.ai.RewardCalculator;
import com.example.traffic.ai.TrafficState;
import com.example.traffic.graph.Graph;
import com.example.traffic.graph.Node;
import com.example.traffic.model.Intersection;
import com.example.traffic.model.TrafficLight;
import com.example.traffic.model.Vehicle;

import java.util.ArrayList;
import java.util.List;

public class SimulationEngine {

    private final List<Vehicle> vehicles = new ArrayList<>();
    private final List<Intersection> intersections = new ArrayList<>();
    private Graph graph;

    // ── Mode IA ──
    private boolean aiMode = false;
    private QLearningAgent agent;
    private double aiDecisionTimer = 0.0;
    private static final double AI_DECISION_INTERVAL = 2.0; // décision toutes les 2 secondes
    private TrafficState lastState;
    private int lastAction = 0;

    public void setGraph(Graph graph) {
        this.graph = graph;
    }

    public Graph getGraph() {
        return graph;
    }

    // ajouter véhicule
    public void addVehicle(Vehicle v) {
        vehicles.add(v);
    }

    // ajouter intersection avec feu
    public void addIntersection(Intersection intersection) {
        intersections.add(intersection);
    }

    public List<Intersection> getIntersections() {
        return intersections;
    }

    // ── Gestion du mode IA ──

    public void setAiMode(boolean aiMode) {
        this.aiMode = aiMode;

        if (aiMode && agent == null) {
            agent = new QLearningAgent();
        }

        // Activer/désactiver le contrôle IA sur tous les feux
        for (Intersection intersection : intersections) {
            intersection.getTrafficLight().setAiControlled(aiMode);
        }
    }

    public boolean isAiMode() {
        return aiMode;
    }

    public QLearningAgent getAgent() {
        return agent;
    }

    // ── Mise à jour de la simulation ──

    public void update(double deltaTime) {

        if (aiMode) {
            updateAI(deltaTime);
        } else {
            // Mode classique : feux automatiques par timer
            for (Intersection intersection : intersections) {
                intersection.getTrafficLight().update(deltaTime);
            }
        }

        // Déplacer les véhicules
        moveVehicles(deltaTime);
    }

    private void updateAI(double deltaTime) {
        aiDecisionTimer += deltaTime;

        if (aiDecisionTimer >= AI_DECISION_INTERVAL) {
            aiDecisionTimer = 0.0;

            TrafficState currentState = TrafficState.fromSimulation(this);

            // Si ce n'est pas le premier pas, on apprend du résultat précédent
            if (lastState != null) {
                double reward = RewardCalculator.calculate(this);
                agent.learn(lastState, lastAction, reward, currentState);
            }

            // L'agent choisit la prochaine action
            int action = agent.chooseAction(currentState);

            // Appliquer l'action aux feux
            applyAction(action);

            lastState = currentState;
            lastAction = action;
        }
    }

    /**
     * Applique l'action de l'IA aux feux.
     * action 0 = Feux Ouest/Est au VERT, Nord/Sud au ROUGE
     * action 1 = Feux Nord/Sud au VERT, Ouest/Est au ROUGE
     */
    private void applyAction(int action) {
        for (Intersection intersection : intersections) {
            String nodeId = intersection.getNode().getId();
            TrafficLight light = intersection.getTrafficLight();

            if (nodeId.startsWith("W") || nodeId.startsWith("E")) {
                light.setState(action == 0 ? TrafficLight.State.GREEN : TrafficLight.State.RED);
            } else {
                light.setState(action == 1 ? TrafficLight.State.GREEN : TrafficLight.State.RED);
            }
        }
    }

    private void moveVehicles(double deltaTime) {
        for (Vehicle v : vehicles) {

            if (v.getCurrentEdge() == null) {
                continue;
            }

            Intersection destinationIntersection =
                    findIntersectionByNode(v.getCurrentEdge().getDestination());

            boolean feuRougeDevant = destinationIntersection != null
                    && !destinationIntersection.getTrafficLight().isGreen();

            double progress = v.getProgress();

            // calculer la prochaine position avant de décider si la voiture doit s'arrêter
            double nextProgress = progress + (v.getSpeed() * deltaTime) / 100.0;

            // Si le feu est rouge et que la voiture va atteindre la zone d'arrêt,
            // elle s'arrête exactement à l'intersection (1.0).
            if (feuRougeDevant && progress <= 1.0 && nextProgress >= 1.0) {
                v.setStopped(true);
                v.setProgress(1.0);
                continue;
            }

            // Si le feu est vert, la voiture avance.
            v.setStopped(false);

            progress = nextProgress;

            // si fin de route
            if (progress >= 1.0) {
                Node dest = v.getCurrentEdge().getDestination();
                v.setCurrentNode(dest);

                if (graph != null) {
                    List<com.example.traffic.graph.Edge> nextRoutes = graph.getNeighbors(dest.getId());
                    if (!nextRoutes.isEmpty()) {
                        // Choisir la route selon la préférence du véhicule
                        int idx = Math.min(v.getPreferredRouteIndex(), nextRoutes.size() - 1);
                        v.setCurrentEdge(nextRoutes.get(idx));
                        progress = 0.0;
                    } else {
                        // Boucle infinie : retéléporter au début
                        v.setCurrentEdge(v.getInitialEdge());
                        v.setCurrentNode(v.getInitialEdge().getSource());
                        progress = 0.0;
                    }
                } else {
                    progress = 1.0;
                }
            }

            v.setProgress(progress);
        }
    }

    private Intersection findIntersectionByNode(Node node) {
        for (Intersection intersection : intersections) {
            if (intersection.getNode().equals(node)) {
                return intersection;
            }
        }
        return null;
    }

    public List<Vehicle> getVehicles() {
        return vehicles;
    }
}
