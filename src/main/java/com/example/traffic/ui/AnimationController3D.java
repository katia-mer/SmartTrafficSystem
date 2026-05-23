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
    private boolean nightMode = false;

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
        Node wE = n(graph, "WE", -1500, 40);
        Node eE = n(graph, "EE", 1500, -40);
        Node nE = n(graph, "NE", -40, -1500);
        Node sE = n(graph, "SE", 40, 1500);

        // Arrêts (avant intersection)
        Node wS = n(graph, "WS", -90, 40);
        Node eS = n(graph, "ES", 90, -40);
        Node nS = n(graph, "NS", -40, -90);
        Node sS = n(graph, "SS", 40, 90);

        // Sorties (bord opposé)
        Node wX = n(graph, "WX", -1500, -40);
        Node eX = n(graph, "EX", 1500, 40);
        Node nX = n(graph, "NX", 40, -1500);
        Node sX = n(graph, "SX", -40, 1500);

        // Coins de virage (dans l'intersection)
        Node cWS = n(graph, "CWS", -40, 40);
        Node cEN = n(graph, "CEN", 40, -40);
        Node cNW = n(graph, "CNW", -40, -40);
        Node cSE = n(graph, "CSE", 40, 40);
        
        Node cWN = n(graph, "CWN", 40, 40);
        Node cES = n(graph, "CES", -40, -40);
        Node cNE = n(graph, "CNE", 40, -40);
        Node cSW = n(graph, "CSW", -40, 40);

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
        e(graph, nS, cWS); e(graph, cWS, eX);    // N→E (Correction: trajet orthogonal via -40, 40)
        e(graph, sS, cEN); e(graph, cEN, wX);    // S→W (Correction: trajet orthogonal via 40, -40)

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

        // Feux: chaque feu face aux voitures (ajustés pour route 160)
        trafficLights3D.add(environment.addTrafficLight(wI.getTrafficLight(), -95, 95, 180));
        trafficLights3D.add(environment.addTrafficLight(eI.getTrafficLight(), 95, -95, 0));
        trafficLights3D.add(environment.addTrafficLight(nI.getTrafficLight(), -95, -95, 270));
        trafficLights3D.add(environment.addTrafficLight(sI.getTrafficLight(), 95, 95, 90));

        // ══ VÉHICULES INITIAUX ══
        car(wE, graph.getNeighbors("WE").get(0), 0.0, randomRoute());
        car(wE, graph.getNeighbors("WE").get(0), 0.15, randomRoute());
        car(eE, graph.getNeighbors("EE").get(0), 0.0, randomRoute());
        car(nE, graph.getNeighbors("NE").get(0), 0.0, randomRoute());
        car(sE, graph.getNeighbors("SE").get(0), 0.1, randomRoute());
    }

    private int randomRoute() {
        int r = random.nextInt(100);
        if (r < 60) return 0;  // Tout droit
        if (r < 80) return 1;  // Droite
        return 2;              // Gauche
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

        Vehicle v = new Vehicle("V" + cars3DMap.size(), start, 30);
        v.setCurrentEdge(init);
        v.setProgress(prog);
        v.setPreferredRouteIndex(route);
        simulationEngine.addVehicle(v);

        Car3D car3d = environment.addCar(v, c1, c2);
        car3d.setNightMode(nightMode);
        cars3DMap.put(v.getId(), car3d);
    }

    public void setNightMode(boolean night) {
        this.nightMode = night;
        for (Car3D car : cars3DMap.values()) {
            car.setNightMode(night);
        }
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
        environment.updateRain(dt);
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
                car3d.setNightMode(nightMode);
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

            // Animation sirène et clignotants
            if (c.isEmergency()) {
                c.updateSiren(dt);
            }
            c.updateTurnSignals(dt);
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
