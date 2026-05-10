package com.example.traffic.simulation;

import com.example.traffic.ai.QLearningAgent;
import com.example.traffic.ai.RewardCalculator;
import com.example.traffic.ai.TrafficState;
import com.example.traffic.graph.Edge;
import com.example.traffic.graph.Graph;
import com.example.traffic.graph.Node;
import com.example.traffic.model.Intersection;
import com.example.traffic.model.TrafficLight;
import com.example.traffic.model.Vehicle;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Moteur de simulation avec :
 * - Spawn dynamique de véhicules (3 niveaux de trafic)
 * - Contrôleur de feux français 8 phases
 * - Gestion des véhicules d'urgence
 * - Modes Pluie, Nuit, Heure de Pointe
 * - Suivi des métriques (temps d'attente par direction)
 */
public class SimulationEngine {

    private final List<Vehicle> vehicles = new ArrayList<>();
    private final List<Intersection> intersections = new ArrayList<>();
    private Graph graph;
    private final Random random = new Random();

    // ── Mode IA ──
    private boolean aiMode = false;
    private QLearningAgent agent;
    private double aiDecisionTimer = 0.0;
    private static final double AI_DECISION_INTERVAL = 2.0;
    private TrafficState lastState;
    private int lastAction = 0;

    // ── Spawn dynamique ──
    public enum TrafficLevel {
        LOW, MED, HIGH
    }

    private TrafficLevel trafficLevel = TrafficLevel.MED;
    private double spawnTimer = 0.0;
    private int maxCars = 40;
    private int vehicleIdCounter = 0;

    // Intervals de spawn (secondes)
    private double getSpawnInterval() {
        double interval;
        switch (trafficLevel) {
            case LOW:
                interval = rushHour ? 0.5 : 2.0;
                break;
            case HIGH:
                interval = rushHour ? 0.15 : 0.4;
                break;
            default:
                interval = rushHour ? 0.3 : 1.0;
                break;
        }
        if (nightMode)
            interval *= 2.5; // Beaucoup moins de voitures la nuit
        return interval;
    }

    // ── Nœuds d'entrée pour spawn ──
    private final List<String> entryNodeIds = new ArrayList<>();

    // ── Contrôleur français 8 phases ──
    private int controllerPhase = 0;
    private double controllerTimer = 10.0;
    private TrafficLight.State nsState = TrafficLight.State.GREEN;
    private TrafficLight.State ewState = TrafficLight.State.RED;

    // ── Modes spéciaux ──
    private boolean rainMode = false;
    private boolean nightMode = false;
    private boolean rushHour = false;

    // ── Véhicules d'urgence ──
    private Vehicle activeEmergency = null;
    private final List<String> aiLog = new ArrayList<>();

    // ── Métriques ──
    private double totalWaitNS = 0.0;
    private double totalWaitEW = 0.0;
    private int waitingCountNS = 0;
    private int waitingCountEW = 0;

    // ── Types d'urgence ──
    public static final String[] EMERGENCY_TYPES = { "ambulance", "fire", "police", "rescue" };
    public static final String[] EMERGENCY_LABELS = { "🚑 Ambulance", "🚒 Pompiers", "🚓 Police", "🚛 Secours" };

    // ══════════════════════════════════════════════════════
    // SETTERS / GETTERS
    // ══════════════════════════════════════════════════════

    public void setGraph(Graph graph) {
        this.graph = graph;
    }

    public Graph getGraph() {
        return graph;
    }

    public void addVehicle(Vehicle v) {
        vehicles.add(v);
    }

    public void addIntersection(Intersection intersection) {
        intersections.add(intersection);
    }

    public List<Intersection> getIntersections() {
        return intersections;
    }

    public List<Vehicle> getVehicles() {
        return vehicles;
    }

    public Vehicle getActiveEmergency() {
        return activeEmergency;
    }

    public void setActiveEmergency(Vehicle v) {
        this.activeEmergency = v;
    }

    public void setTrafficLevel(TrafficLevel level) {
        this.trafficLevel = level;
    }

    public TrafficLevel getTrafficLevel() {
        return trafficLevel;
    }

    public boolean isRainMode() {
        return rainMode;
    }

    public boolean isNightMode() {
        return nightMode;
    }

    public boolean isRushHour() {
        return rushHour;
    }

    public TrafficLight.State getNsState() {
        return nsState;
    }

    public TrafficLight.State getEwState() {
        return ewState;
    }

    public double getAvgWaitNS() {
        return waitingCountNS > 0 ? totalWaitNS / waitingCountNS : 0;
    }

    public double getAvgWaitEW() {
        return waitingCountEW > 0 ? totalWaitEW / waitingCountEW : 0;
    }

    public int getWaitingCountNS() {
        return waitingCountNS;
    }

    public int getWaitingCountEW() {
        return waitingCountEW;
    }

    public List<String> getAiLog() {
        return aiLog;
    }

    public void addEntryNodeId(String nodeId) {
        entryNodeIds.add(nodeId);
    }

    // ══════════════════════════════════════════════════════
    // MODES SPÉCIAUX
    // ══════════════════════════════════════════════════════

    public void setRainMode(boolean rain) {
        this.rainMode = rain;
        updateAllVehicleSpeeds();
        logAI(rain ? "🌧️ Mode Pluie — Vitesse réduite & Orange allongé" : "☀️ Mode Pluie désactivé");
    }

    public void setNightMode(boolean night) {
        this.nightMode = night;
        updateAllVehicleSpeeds();
        logAI(night ? "🌙 Mode Nuit — Cycles de feux courts" : "☀️ Mode Jour restauré");
    }

    private void updateAllVehicleSpeeds() {
        double modifier = calculateTotalModifier();
        for (Vehicle v : vehicles) {
            if (!v.isEmergency())
                v.setSpeedModifier(modifier);
        }
    }

    private double calculateTotalModifier() {
        double modifier = 1.0;
        if (rainMode)
            modifier *= 0.65; // Pluie = -35% vitesse
        if (nightMode)
            modifier *= 0.85; // Nuit = -15% vitesse
        return modifier;
    }

    public void setRushHour(boolean rush) {
        this.rushHour = rush;
        this.maxCars = rush ? 60 : 40;
        logAI(rush ? "🚗🚗🚗 Heure de Pointe — Trafic dense!" : "🚗 Trafic normal restauré");
    }

    // ══════════════════════════════════════════════════════
    // MODE IA
    // ══════════════════════════════════════════════════════

    public void setAiMode(boolean aiMode) {
        this.aiMode = aiMode;
        if (aiMode && agent == null) {
            agent = new QLearningAgent();
        }
        for (Intersection intersection : intersections) {
            intersection.getTrafficLight().setAiControlled(aiMode);
        }
        logAI(aiMode ? "🤖 IA Q-Learning activée" : "⭕ IA désactivée — Timer classique");
    }

    public boolean isAiMode() {
        return aiMode;
    }

    public QLearningAgent getAgent() {
        return agent;
    }

    // ══════════════════════════════════════════════════════
    // MISE À JOUR PRINCIPALE
    // ══════════════════════════════════════════════════════

    public void update(double deltaTime) {
        // 1. Contrôle des feux
        if (aiMode) {
            updateAI(deltaTime);
        } else {
            updateFrenchController(deltaTime);
        }

        // 2. Spawn dynamique
        updateSpawn(deltaTime);

        // 3. Déplacer les véhicules
        moveVehicles(deltaTime);

        // 4. Mise à jour des métriques
        updateMetrics(deltaTime);

        // 5. Nettoyage véhicules hors limites
        cleanupVehicles();
    }

    // ══════════════════════════════════════════════════════
    // CONTRÔLEUR FRANÇAIS 8 PHASES
    // ══════════════════════════════════════════════════════

    private void updateFrenchController(double deltaTime) {
        // Logique adaptative : si une rue est très chargée, on accélère le passage au vert
        double timeMultiplier = 1.0;
        
        if (nsState == TrafficLight.State.RED && waitingCountNS > waitingCountEW + 2) {
            timeMultiplier = 1.5 + (waitingCountNS * 0.1); // Plus il y a de monde, plus ça va vite
        } else if (ewState == TrafficLight.State.RED && waitingCountEW > waitingCountNS + 2) {
            timeMultiplier = 1.5 + (waitingCountEW * 0.1);
        }
        
        // Limiter le multiplicateur pour garder un minimum de réalisme
        timeMultiplier = Math.min(timeMultiplier, 4.0);

        controllerTimer -= (deltaTime * timeMultiplier);
        
        if (controllerTimer <= 0) {
            nextControllerPhase();
        }
        applyControllerToLights();
    }

    private void nextControllerPhase() {
        controllerPhase = (controllerPhase + 1) % 8;

        // --- Logique de Durée Dynamique ---
        // Nuit : cycles courts car moins de monde
        // Pluie : orange plus long (freinage) et sécurité accrue
        double greenDuration = nightMode ? 5.0 : 10.0;
        double yellowDuration = rainMode ? 4.5 : 3.0;   // Plus long sous la pluie pour le freinage
        double allRedDuration = rainMode ? 2.5 : 1.0;   // Plus de sécurité pour vider l'intersection
        double redYellowDuration = 2.0;

        switch (controllerPhase) {
            case 0:
                nsState = TrafficLight.State.GREEN;
                ewState = TrafficLight.State.RED;
                controllerTimer = greenDuration;
                break;
            case 1:
                nsState = TrafficLight.State.YELLOW;
                ewState = TrafficLight.State.RED;
                controllerTimer = yellowDuration;
                break;
            case 2:
                nsState = TrafficLight.State.RED;
                ewState = TrafficLight.State.RED;
                controllerTimer = allRedDuration;
                break;
            case 3:
                nsState = TrafficLight.State.RED;
                ewState = TrafficLight.State.RED_YELLOW;
                controllerTimer = redYellowDuration;
                break;
            case 4:
                nsState = TrafficLight.State.RED;
                ewState = TrafficLight.State.GREEN;
                controllerTimer = greenDuration;
                break;
            case 5:
                nsState = TrafficLight.State.RED;
                ewState = TrafficLight.State.YELLOW;
                controllerTimer = yellowDuration;
                break;
            case 6:
                nsState = TrafficLight.State.RED;
                ewState = TrafficLight.State.RED;
                controllerTimer = allRedDuration;
                break;
            case 7:
                nsState = TrafficLight.State.RED_YELLOW;
                ewState = TrafficLight.State.RED;
                controllerTimer = redYellowDuration;
                break;
        }
    }

    private void applyControllerToLights() {
        for (Intersection intersection : intersections) {
            String nodeId = intersection.getNode().getId();
            TrafficLight light = intersection.getTrafficLight();

            if (nodeId.startsWith("N") || nodeId.startsWith("S")) {
                light.setState(nsState);
            } else {
                light.setState(ewState);
            }
        }
    }

    /** Force le vert N/S (urgence) */
    public void forceGreenNS() {
        nsState = TrafficLight.State.GREEN;
        ewState = TrafficLight.State.RED;
        controllerPhase = 0;
        controllerTimer = 20.0;
        applyControllerToLights();
        logAI("🚨 URGENCE: Vert forcé N/S");
    }

    /** Force le vert E/O (urgence) */
    public void forceGreenEW() {
        nsState = TrafficLight.State.RED;
        ewState = TrafficLight.State.GREEN;
        controllerPhase = 4;
        controllerTimer = 20.0;
        applyControllerToLights();
        logAI("🚨 URGENCE: Vert forcé E/O");
    }

    /** Appelé par l'IA pour forcer une phase */
    public void setPhase(TrafficLight.State ns, TrafficLight.State ew) {
        this.nsState = ns;
        this.ewState = ew;
        applyControllerToLights();
    }

    // ══════════════════════════════════════════════════════
    // SPAWN DYNAMIQUE
    // ══════════════════════════════════════════════════════

    private void updateSpawn(double deltaTime) {
        if (entryNodeIds.isEmpty() || graph == null)
            return;

        spawnTimer += deltaTime;
        double interval = getSpawnInterval();

        if (spawnTimer >= interval && vehicles.size() < maxCars) {
            spawnTimer = 0;
            spawnVehicle();
        }
    }

    private void spawnVehicle() {
        if (entryNodeIds.isEmpty())
            return;

        String entryId = entryNodeIds.get(random.nextInt(entryNodeIds.size()));
        Node entryNode = graph.getNode(entryId);
        if (entryNode == null)
            return;

        List<Edge> edges = graph.getNeighbors(entryId);
        if (edges.isEmpty())
            return;

        // Vérifier qu'il n'y a pas de voiture trop proche de l'entrée
        for (Vehicle v : vehicles) {
            if (v.getCurrentEdge() != null &&
                    v.getCurrentEdge().getSource().getId().equals(entryId) &&
                    v.getProgress() < 0.15) {
                return; // Trop proche, on attend
            }
        }

        double speed = 25 + random.nextInt(15); // 25-40

        Vehicle v = new Vehicle("V" + (vehicleIdCounter++), entryNode, speed);
        v.setCurrentEdge(edges.get(0));
        v.setProgress(0.0);
        v.setPreferredRouteIndex(0); // Toujours tout droit comme avant

        v.setSpeedModifier(calculateTotalModifier());

        vehicles.add(v);
    }

    public void spawnVehicleWithType(Vehicle.VehicleType type) {
        if (entryNodeIds.isEmpty() || graph == null) return;
        
        String entryId = entryNodeIds.get(random.nextInt(entryNodeIds.size()));
        Node entryNode = graph.getNode(entryId);
        List<Edge> edges = graph.getNeighbors(entryId);
        if (edges.isEmpty()) return;

        double speed = 25 + random.nextInt(15);
        Vehicle v = new Vehicle("V" + (vehicleIdCounter++), entryNode, speed, type);
        v.setCurrentEdge(edges.get(0));
        v.setProgress(0.0);
        v.setPreferredRouteIndex(0);
        v.setSpeedModifier(calculateTotalModifier());

        vehicles.add(v);
        logAI("➕ Nouveau " + type + " lancé (" + entryId + ")");
    }

    /** Spawn un véhicule d'urgence sur une direction aléatoire */
    public Vehicle spawnEmergencyVehicle() {
        if (activeEmergency != null)
            return null;
        if (entryNodeIds.isEmpty())
            return null;

        String entryId = entryNodeIds.get(random.nextInt(entryNodeIds.size()));
        Node entryNode = graph.getNode(entryId);
        if (entryNode == null)
            return null;

        List<Edge> edges = graph.getNeighbors(entryId);
        if (edges.isEmpty())
            return null;

        String emType = EMERGENCY_TYPES[random.nextInt(EMERGENCY_TYPES.length)];
        String emLabel = EMERGENCY_LABELS[java.util.Arrays.asList(EMERGENCY_TYPES).indexOf(emType)];

        Vehicle em = new Vehicle("EM" + (vehicleIdCounter++), entryNode, 120, emType);
        em.setCurrentEdge(edges.get(0));
        em.setProgress(0.0);
        em.setPreferredRouteIndex(0);

        vehicles.add(em);
        activeEmergency = em;

        // Forcer le vert pour la direction de l'urgence
        boolean isNS = entryId.startsWith("N") || entryId.startsWith("S");
        if (isNS)
            forceGreenNS();
        else
            forceGreenEW();

        logAI("🚨 ALERTE: " + emLabel + " lancé! Direction " + entryId);
        return em;
    }

    /** Spawn une double urgence séquentielle */
    public void spawnDoubleEmergency() {
        spawnEmergencyVehicle();
        // On lance une deuxième urgence après 2 secondes
        new Thread(() -> {
            try { Thread.sleep(2000); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
            javafx.application.Platform.runLater(this::spawnEmergencyVehicle);
        }).start();
    }

    // ══════════════════════════════════════════════════════
    // IA Q-LEARNING
    // ══════════════════════════════════════════════════════

    private void updateAI(double deltaTime) {
        aiDecisionTimer += deltaTime;
        if (aiDecisionTimer >= AI_DECISION_INTERVAL) {
            aiDecisionTimer = 0.0;

            TrafficState currentState = TrafficState.fromSimulation(this);

            if (lastState != null) {
                double reward = RewardCalculator.calculate(this);
                agent.learn(lastState, lastAction, reward, currentState);
            }

            int action = agent.chooseAction(currentState);
            applyAction(action);

            lastState = currentState;
            lastAction = action;

            logAI("🤖 IA: " + agent.getLastDecisionReason());
        }
    }

    private void applyAction(int action) {
        if (action == 0) {
            nsState = TrafficLight.State.RED;
            ewState = TrafficLight.State.GREEN;
        } else {
            nsState = TrafficLight.State.GREEN;
            ewState = TrafficLight.State.RED;
        }
        applyControllerToLights();
    }

    // ══════════════════════════════════════════════════════
    // DÉPLACEMENT DES VÉHICULES (avec distance de sécurité)
    // ══════════════════════════════════════════════════════

    /** Distance de sécurité minimale en unités absolues */
    public double getSafeDistance(Edge edge) {
        if (edge == null) return 0.1;
        // Une voiture fait environ 60 unités de long (du pare-chocs avant au pare-chocs arrière).
        // 90 unités = 1 voiture + 30 unités d'espace (environ une demi-voiture d'espace)
        double minGap = rainMode ? 120.0 : 90.0; 
        return minGap / edge.getLength();
    }

    private void moveVehicles(double deltaTime) {
        for (Vehicle v : vehicles) {
            if (v.getCurrentEdge() == null)
                continue;

            // 1. Vérifier le feu
            Intersection destIntersection = findIntersectionByNode(v.getCurrentEdge().getDestination());
            TrafficLight light = (destIntersection != null) ? destIntersection.getTrafficLight() : null;
            
            boolean isRed = light != null && (light.isRed() || light.isRedYellow());
            boolean isYellow = light != null && light.isYellow();
            
            if (v.isEmergency()) {
                isRed = false;
                isYellow = false;
            }

            double progress = v.getProgress();
            double effectiveSpeed = v.getEffectiveSpeed();

            // Ralentir à l'orange si on approche de l'intersection
            if (isYellow && progress > 0.65) {
                effectiveSpeed *= 0.5;
            }

            // 2. Trouver la voiture devant
            double distToCarAhead = findDistanceToCarAhead(v);
            double safeDist = getSafeDistance(v.getCurrentEdge());
            // 3. Limiter par la voiture devant (distance de sécurité)
            if (distToCarAhead < safeDist * 2) {
                // Trop proche → ralentir ou s'arrêter
                if (distToCarAhead <= safeDist) {
                    v.setStopped(true);
                    v.addWaitTime(deltaTime);
                    continue; // Ne pas bouger du tout
                }
                // Ralentir proportionnellement à la distance
                double slowFactor = (distToCarAhead - safeDist) / safeDist;
                effectiveSpeed *= Math.max(0.1, slowFactor);
            }

            double nextProgress = progress + (effectiveSpeed * deltaTime) / 100.0;

            // 4. Arrêter au feu rouge (rester arrêté tant que c'est rouge)
            if (isRed && progress <= 0.96 && nextProgress >= 0.96) {
                v.setStopped(true);
                v.setProgress(0.96);
                v.addWaitTime(deltaTime);
                continue;
            }

            // 6. Limiter par la voiture devant
            if (distToCarAhead < Double.MAX_VALUE) {
                double safeDistVal = getSafeDistance(v.getCurrentEdge());
                double maxAllowed = v.getProgress() + (distToCarAhead - safeDistVal);
                if (nextProgress > maxAllowed && nextProgress < 1.0) {
                    nextProgress = Math.max(progress, maxAllowed);
                    if (nextProgress <= progress) {
                        v.setStopped(true);
                        v.addWaitTime(deltaTime);
                        continue;
                    }
                }
            }

            v.setStopped(false);
            progress = nextProgress;

            // 7. Fin d'arête → passer à la suivante
            if (progress >= 1.0) {
                Node dest = v.getCurrentEdge().getDestination();
                v.setCurrentNode(dest);

                if (graph != null) {
                    List<Edge> nextRoutes = graph.getNeighbors(dest.getId());
                    if (!nextRoutes.isEmpty()) {
                        int idx = Math.min(v.getPreferredRouteIndex(), nextRoutes.size() - 1);
                        v.setCurrentEdge(nextRoutes.get(idx));
                        progress = 0.0;
                    } else {
                        // Fin du trajet
                        if (v.isEmergency()) {
                            v.setMarkedForRemoval(true);
                        } else {
                            v.setMarkedForRemoval(true); // Supprimer la voiture en fin de route
                        }
                        continue;
                    }
                } else {
                    progress = 1.0;
                }
            }
            v.setProgress(progress);
        }
    }

    /**
     * Trouve la distance (en progress) à la voiture la plus proche devant sur la
     * même arête
     */
    private double findDistanceToCarAhead(Vehicle current) {
        Edge currentEdge = current.getCurrentEdge();
        if (currentEdge == null)
            return Double.MAX_VALUE;
 
        double minDist = Double.MAX_VALUE;
        for (Vehicle other : vehicles) {
            if (other == current || other.getCurrentEdge() == null)
                continue;
 
            Edge otherEdge = other.getCurrentEdge();
 
            // Cas 1 : Même arête et devant
            if (otherEdge == currentEdge && other.getProgress() > current.getProgress()) {
                double dist = other.getProgress() - current.getProgress();
                if (dist < minDist)
                    minDist = dist;
            }
            
            // Cas 2 : La voiture est déjà sur l'arête suivante (intersection)
            // On vérifie si la destination de notre arête est le départ de l'arête de l'autre
            else if (currentEdge.getDestination().equals(otherEdge.getSource())) {
                // On calcule la distance combinée en unités de "progress" de l'arête actuelle
                // dist = (ce qu'il reste sur l'arête A) + (ce qui est déjà parcouru sur l'arête B, converti)
                double remainingA = 1.0 - current.getProgress();
                double progressB_in_A = (other.getProgress() * otherEdge.getLength()) / currentEdge.getLength();
                double totalDist = remainingA + progressB_in_A;
                
                if (totalDist < minDist)
                    minDist = totalDist;
            }
        }
        return minDist;
    }

    /** Récupère la progress de la voiture la plus proche devant */
    private double getCarAheadProgress(Vehicle current) {
        Edge currentEdge = current.getCurrentEdge();
        if (currentEdge == null)
            return Double.MAX_VALUE;

        double closestProgress = Double.MAX_VALUE;
        double closestDist = Double.MAX_VALUE;
        for (Vehicle other : vehicles) {
            if (other == current || other.getCurrentEdge() == null)
                continue;
            if (other.getCurrentEdge() == currentEdge && other.getProgress() > current.getProgress()) {
                double dist = other.getProgress() - current.getProgress();
                if (dist < closestDist) {
                    closestDist = dist;
                    closestProgress = other.getProgress();
                }
            }
        }
        return closestProgress;
    }

    // ══════════════════════════════════════════════════════
    // MÉTRIQUES
    // ══════════════════════════════════════════════════════

    private void updateMetrics(double deltaTime) {
        waitingCountNS = 0;
        waitingCountEW = 0;
        totalWaitNS = 0;
        totalWaitEW = 0;

        for (Vehicle v : vehicles) {
            if (v.isEmergency())
                continue;
            if (v.isStopped() || v.getProgress() >= 0.8) {
                if (v.getCurrentEdge() == null)
                    continue;
                String edgeId = v.getCurrentEdge().getSource().getId();
                if (edgeId.startsWith("N") || edgeId.startsWith("S")) {
                    waitingCountNS++;
                    totalWaitNS += v.getWaitTime();
                } else {
                    waitingCountEW++;
                    totalWaitEW += v.getWaitTime();
                }
            }
        }
    }

    // ══════════════════════════════════════════════════════
    // NETTOYAGE
    // ══════════════════════════════════════════════════════

    private void cleanupVehicles() {
        vehicles.removeIf(v -> {
            if (v.isMarkedForRemoval()) {
                if (v == activeEmergency) {
                    activeEmergency = null;
                    logAI("✅ Véhicule d'urgence a quitté l'intersection");
                }
                return true;
            }
            return false;
        });
    }

    // ══════════════════════════════════════════════════════
    // UTILITAIRES
    // ══════════════════════════════════════════════════════

    private Intersection findIntersectionByNode(Node node) {
        for (Intersection intersection : intersections) {
            if (intersection.getNode().equals(node))
                return intersection;
        }
        return null;
    }

    public void logAI(String message) {
        String timestamp = java.time.LocalTime.now().format(java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss"));
        aiLog.add(0, "[" + timestamp + "] " + message);
        if (aiLog.size() > 15)
            aiLog.remove(aiLog.size() - 1);
    }

    /** Réinitialisation complète */
    public void resetAll() {
        vehicles.clear();
        activeEmergency = null;
        totalWaitNS = 0;
        totalWaitEW = 0;
        waitingCountNS = 0;
        waitingCountEW = 0;
        spawnTimer = 0;
        vehicleIdCounter = 0;
        controllerPhase = 0;
        controllerTimer = 10.0;
        nsState = TrafficLight.State.GREEN;
        ewState = TrafficLight.State.RED;
        aiLog.clear();
        if (agent != null)
            agent.reset();
        logAI("↻ Simulation réinitialisée");
    }
}
