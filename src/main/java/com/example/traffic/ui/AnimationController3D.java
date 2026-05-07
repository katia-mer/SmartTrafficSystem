package com.example.traffic.ui;

import com.example.traffic.graph.Edge;
import com.example.traffic.graph.Graph;
import com.example.traffic.graph.Node;
import com.example.traffic.model.Intersection;
import com.example.traffic.model.Vehicle;
import com.example.traffic.simulation.SimulationEngine;
import com.example.traffic.ui.models.Car3D;
import com.example.traffic.ui.models.TrafficLight3D;
import javafx.scene.paint.Color;

import java.util.*;

/**
 * Contrôleur d'animation 3D avec :
 * - Spawn dynamique et suppression de véhicules
 * - Véhicules d'urgence avec sirène animée
 * - Synchronisation moteur ↔ scène 3D
 */
public class AnimationController3D {

    private final SimulationEngine simulationEngine;
    private final Map<String, Car3D> cars3DMap = new LinkedHashMap<>();
    private final List<TrafficLight3D> trafficLights3D = new ArrayList<>();
    private final CityEnvironment environment;
    private static final double CAR_Y = 38;
    private final Random random = new Random();

    // Couleurs aléatoires pour les voitures
    private static final Color[][] CAR_COLORS = {
            {Color.rgb(30, 120, 220), Color.rgb(60, 160, 255)},
            {Color.rgb(230, 120, 30), Color.rgb(255, 170, 60)},
            {Color.rgb(200, 30, 50), Color.rgb(240, 80, 100)},
            {Color.rgb(50, 180, 80), Color.rgb(80, 220, 110)},
            {Color.rgb(180, 60, 150), Color.rgb(220, 100, 190)},
            {Color.rgb(120, 80, 200), Color.rgb(160, 120, 240)},
            {Color.rgb(30, 160, 220), Color.rgb(80, 200, 255)},
            {Color.rgb(220, 200, 30), Color.rgb(255, 240, 80)},
            {Color.rgb(255, 100, 100), Color.rgb(255, 160, 160)},
            {Color.rgb(100, 200, 200), Color.rgb(150, 230, 230)},
            {Color.rgb(226, 232, 240), Color.rgb(200, 210, 225)},
            {Color.rgb(30, 41, 59), Color.rgb(60, 70, 90)},
    };

    public AnimationController3D(CityEnvironment environment) {
        this.environment = environment;
        this.simulationEngine = new SimulationEngine();
        buildLogicAndVisuals();
    }

    private void buildLogicAndVisuals() {
        Graph graph = new Graph();
        simulationEngine.setGraph(graph);

        // ══ NOEUDS ══
        // Entrées (bord de carte)
        Node wE = n(graph, "WE", -440, 25);
        Node eE = n(graph, "EE", 440, -25);
        Node nE = n(graph, "NE", -25, -440);
        Node sE = n(graph, "SE", 25, 440);

        // Arrêts (avant intersection)
        Node wS = n(graph, "WS", -80, 25);
        Node eS = n(graph, "ES", 80, -25);
        Node nS = n(graph, "NS", -25, -80);
        Node sS = n(graph, "SS", 25, 80);

        // Sorties (bord opposé)
        Node wX = n(graph, "WX", -440, -25);
        Node eX = n(graph, "EX", 440, 25);
        Node nX = n(graph, "NX", 25, -440);
        Node sX = n(graph, "SX", -25, 440);

        // Coins de virage (dans l'intersection)
        Node cWS = n(graph, "CWS", -25, 25);
        Node cEN = n(graph, "CEN", 25, -25);
        Node cNW = n(graph, "CNW", -25, -25);
        Node cSE = n(graph, "CSE", 25, 25);
        Node cWN = n(graph, "CWN", 25, 25);
        Node cES = n(graph, "CES", -25, -25);

        // ══ ROUTES (Edges) ══
        // Approches
        e(graph, wE, wS);
        e(graph, eE, eS);
        e(graph, nE, nS);
        e(graph, sE, sS);

        // Tout droit
        e(graph, wS, eX);
        e(graph, eS, wX);
        e(graph, nS, sX);
        e(graph, sS, nX);

        // Virage à droite
        e(graph, wS, cWS); e(graph, cWS, sX);   // W→S
        e(graph, eS, cEN); e(graph, cEN, nX);    // E→N
        e(graph, nS, cNW); e(graph, cNW, wX);    // N→W
        e(graph, sS, cSE); e(graph, cSE, eX);    // S→E

        // Virage à gauche
        e(graph, wS, cWN); e(graph, cWN, nX);    // W→N
        e(graph, eS, cES); e(graph, cES, sX);    // E→S
        // Ajout des virages à gauche manquants pour N et S
        Node cNE = n(graph, "CNE", 25, -25);      // N→E
        Node cSW = n(graph, "CSW", -25, 25);      // S→W
        e(graph, nS, cNE); e(graph, cNE, eX);    // N→E (virage à gauche)
        e(graph, sS, cSW); e(graph, cSW, wX);    // S→W (virage à gauche)

        // ══ Enregistrer les nœuds d'entrée pour le spawn ══
        simulationEngine.addEntryNodeId("WE");
        simulationEngine.addEntryNodeId("EE");
        simulationEngine.addEntryNodeId("NE");
        simulationEngine.addEntryNodeId("SE");

        // ══ FEUX TRICOLORES (orientés face aux voitures) ══
        Intersection wI = new Intersection(wS);
        Intersection eI = new Intersection(eS);
        Intersection nI = new Intersection(nS);
        Intersection sI = new Intersection(sS);
        simulationEngine.addIntersection(wI);
        simulationEngine.addIntersection(eI);
        simulationEngine.addIntersection(nI);
        simulationEngine.addIntersection(sI);

        // Feux: chaque feu fait face aux voitures qui arrivent et est placé sur le trottoir à DROITE
        // W → Voitures venant de l'Ouest (roulant sur z > 0). Feu à droite (z = 75), avant l'intersection (x = -85)
        trafficLights3D.add(environment.addTrafficLight(wI.getTrafficLight(), -85, 75, 180));
        
        // E → Voitures venant de l'Est (roulant sur z < 0). Feu à droite (z = -75), avant l'intersection (x = 85)
        trafficLights3D.add(environment.addTrafficLight(eI.getTrafficLight(), 85, -75, 0));
        
        // N → Voitures venant du Nord (roulant sur x < 0). Feu à droite (x = -75), avant l'intersection (z = -85)
        trafficLights3D.add(environment.addTrafficLight(nI.getTrafficLight(), -75, -85, 270));
        
        // S → Voitures venant du Sud (roulant sur x > 0). Feu à droite (x = 75), avant l'intersection (z = 85)
        trafficLights3D.add(environment.addTrafficLight(sI.getTrafficLight(), 75, 85, 90));

        // ══ VÉHICULES INITIAUX ══
        car(wE, graph.getNeighbors("WE").get(0), 0.0, 0);
        car(wE, graph.getNeighbors("WE").get(0), 0.15, 0);
        car(eE, graph.getNeighbors("EE").get(0), 0.0, 0);
        car(nE, graph.getNeighbors("NE").get(0), 0.0, 0);
        car(sE, graph.getNeighbors("SE").get(0), 0.1, 0);
    }

    // Helpers
    private Node n(Graph g, String id, double x, double y) {
        Node node = new Node(id, x, y);
        g.addNode(node);
        return node;
    }

    private Edge e(Graph g, Node src, Node dst) {
        Edge edge = new Edge(src, dst, 50);
        g.addEdge(edge);
        return edge;
    }

    private void car(Node start, Edge init, double prog, int route) {
        int colorIdx = random.nextInt(CAR_COLORS.length);
        Color c1 = CAR_COLORS[colorIdx][0];
        Color c2 = CAR_COLORS[colorIdx][1];

        Vehicle v = new Vehicle("V" + cars3DMap.size(), start, 90);
        v.setCurrentEdge(init);
        v.setProgress(prog);
        v.setPreferredRouteIndex(route);
        simulationEngine.addVehicle(v);

        Car3D car3d = environment.addCar(v, c1, c2);
        cars3DMap.put(v.getId(), car3d);
    }

    // ══ CONTRÔLE ══

    public void reset() {
        // Supprimer toutes les voitures 3D
        for (Car3D c : cars3DMap.values()) {
            environment.removeCar(c);
        }
        cars3DMap.clear();

        // Réinitialiser le moteur
        simulationEngine.resetAll();

        updateTrafficLights();
    }

    public void update(double dt) {
        simulationEngine.update(dt);

        // Synchroniser les voitures 3D
        syncCars3D(dt);

        updateTrafficLights();
        updateCars3D(dt);
    }

    /** Synchronise la liste de voitures 3D avec le moteur de simulation */
    private void syncCars3D(double dt) {
        List<Vehicle> engineVehicles = simulationEngine.getVehicles();

        // 1. Ajouter les nouvelles voitures
        for (Vehicle v : engineVehicles) {
            if (!cars3DMap.containsKey(v.getId())) {
                Car3D car3d;
                if (v.isEmergency()) {
                    car3d = environment.addEmergencyCar(v);
                } else {
                    int colorIdx = random.nextInt(CAR_COLORS.length);
                    car3d = environment.addCar(v, CAR_COLORS[colorIdx][0], CAR_COLORS[colorIdx][1]);
                }
                cars3DMap.put(v.getId(), car3d);
            }
        }

        // 2. Supprimer les voitures qui ne sont plus dans le moteur
        Set<String> engineIds = new HashSet<>();
        for (Vehicle v : engineVehicles) {
            engineIds.add(v.getId());
        }

        Iterator<Map.Entry<String, Car3D>> it = cars3DMap.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, Car3D> entry = it.next();
            if (!engineIds.contains(entry.getKey())) {
                environment.removeCar(entry.getValue());
                it.remove();
            }
        }
    }

    public void updateTrafficLights() {
        for (TrafficLight3D tl : trafficLights3D) tl.updateVisual();
    }

    public void updateCars3D(double dt) {
        for (Car3D c : cars3DMap.values()) {
            Vehicle v = c.getVehicle();
            double angle = 0;
            Edge edge = v.getCurrentEdge();
            if (edge != null) {
                double dx = edge.getDestination().getX() - edge.getSource().getX();
                double dy = edge.getDestination().getY() - edge.getSource().getY();
                angle = Math.toDegrees(Math.atan2(dy, dx));
            }
            c.setPosition(v.getX(), CAR_Y, v.getY(), angle);

            // Animation sirène
            if (c.isEmergency()) {
                c.updateSiren(dt);
            }
        }
    }

    // Backward compatibility
    public void updateCars3D() {
        updateCars3D(0.016);
    }

    public SimulationEngine getSimulationEngine() {
        return simulationEngine;
    }

    public int getActiveCarsCount() {
        return cars3DMap.size();
    }
}
