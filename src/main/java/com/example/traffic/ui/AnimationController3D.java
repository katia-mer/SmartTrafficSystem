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

import java.util.ArrayList;
import java.util.List;

public class AnimationController3D {

    private final SimulationEngine simulationEngine;
    private final List<Car3D> cars3D = new ArrayList<>();
    private final List<TrafficLight3D> trafficLights3D = new ArrayList<>();
    private final CityEnvironment environment;
    private static final double CAR_Y = 38;

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

        // Coins de virage (dans l'intersection, positionnés sur la bonne voie)
        Node cWS = n(graph, "CWS", -25, 25);   // W→S : virage à droite
        Node cEN = n(graph, "CEN", 25, -25);    // E→N : virage à droite
        Node cNW = n(graph, "CNW", -25, -25);   // N→W : virage à droite
        Node cSE = n(graph, "CSE", 25, 25);     // S→E : virage à droite
        Node cWN = n(graph, "CWN", 25, 25);     // W→N : virage à gauche
        Node cES = n(graph, "CES", -25, -25);   // E→S : virage à gauche

        // ══ ROUTES (Edges) ══
        // Approches
        Edge waE = e(graph, wE, wS);
        Edge eaE = e(graph, eE, eS);
        Edge naE = e(graph, nE, nS);
        Edge saE = e(graph, sE, sS);

        // Tout droit (index 0 dans getNeighbors)
        e(graph, wS, eX);  // W→E tout droit
        e(graph, eS, wX);  // E→W tout droit
        e(graph, nS, sX);  // N→S tout droit
        e(graph, sS, nX);  // S→N tout droit

        // Virage à droite (index 1)
        e(graph, wS, cWS); e(graph, cWS, sX);  // W→S
        e(graph, eS, cEN); e(graph, cEN, nX);   // E→N
        e(graph, nS, cNW); e(graph, cNW, wX);   // N→W
        e(graph, sS, cSE); e(graph, cSE, eX);   // S→E

        // Virage à gauche (index 2)
        e(graph, wS, cWN); e(graph, cWN, nX);   // W→N
        e(graph, eS, cES); e(graph, cES, sX);   // E→S

        // ══ FEUX TRICOLORES ══
        Intersection wI = new Intersection(wS);
        Intersection eI = new Intersection(eS);
        Intersection nI = new Intersection(nS);
        Intersection sI = new Intersection(sS);
        simulationEngine.addIntersection(wI);
        simulationEngine.addIntersection(eI);
        simulationEngine.addIntersection(nI);
        simulationEngine.addIntersection(sI);

        trafficLights3D.add(environment.addTrafficLight(wI.getTrafficLight(), -100, -80));
        trafficLights3D.add(environment.addTrafficLight(eI.getTrafficLight(), 100, 80));
        trafficLights3D.add(environment.addTrafficLight(nI.getTrafficLight(), 80, -100));
        trafficLights3D.add(environment.addTrafficLight(sI.getTrafficLight(), -80, 100));

        // ══ VÉHICULES (tous sur la route visible, progress 0.0 à 0.45) ══
        // W→E tout droit (dense)
        car(wE, waE, 0.0, 0, Color.rgb(30,120,220), Color.rgb(60,160,255));
        car(wE, waE, 0.15, 0, Color.rgb(230,120,30), Color.rgb(255,170,60));
        car(wE, waE, 0.35, 0, Color.rgb(200,30,50), Color.rgb(240,80,100));

        // E→W tout droit (moyen)
        car(eE, eaE, 0.0, 0, Color.rgb(50,180,80), Color.rgb(80,220,110));
        car(eE, eaE, 0.2, 0, Color.rgb(180,60,150), Color.rgb(220,100,190));

        // N→S tout droit (moyen)
        car(nE, naE, 0.0, 0, Color.rgb(120,80,200), Color.rgb(160,120,240));
        car(nE, naE, 0.25, 0, Color.rgb(30,160,220), Color.rgb(80,200,255));

        // S→N tout droit (faible)
        car(sE, saE, 0.1, 0, Color.rgb(220,200,30), Color.rgb(255,240,80));

        // Virage à droite : W→S
        car(wE, waE, 0.45, 1, Color.rgb(255,100,100), Color.rgb(255,160,160));

        // Virage à droite : E→N
        car(eE, eaE, 0.4, 1, Color.rgb(200,180,100), Color.rgb(240,220,140));

        // Virage à gauche : W→N
        car(wE, waE, 0.55, 2, Color.rgb(100,200,200), Color.rgb(150,230,230));
    }

    // Helpers
    private Node n(Graph g, String id, double x, double y) {
        Node node = new Node(id, x, y); g.addNode(node); return node;
    }
    private Edge e(Graph g, Node src, Node dst) {
        Edge edge = new Edge(src, dst, 50); g.addEdge(edge); return edge;
    }
    private void car(Node start, Edge init, double prog, int route, Color c1, Color c2) {
        Vehicle v = new Vehicle("V" + cars3D.size(), start, 90);
        v.setCurrentEdge(init);
        v.setProgress(prog);
        v.setPreferredRouteIndex(route);
        simulationEngine.addVehicle(v);
        cars3D.add(environment.addCar(v, c1, c2));
    }

    // ══ CONTRÔLE ══

    public void reset() {
        for (Car3D c : cars3D) {
            Vehicle v = c.getVehicle();
            v.setCurrentEdge(v.getInitialEdge());
            v.setProgress(0.0);
            v.setStopped(false);
        }
        if (simulationEngine.getAgent() != null) simulationEngine.getAgent().reset();
        updateCars3D();
        updateTrafficLights();
    }

    public void update(double dt) {
        simulationEngine.update(dt);
        updateTrafficLights();
        updateCars3D();
    }

    public void updateTrafficLights() {
        for (TrafficLight3D tl : trafficLights3D) tl.updateVisual();
    }

    public void updateCars3D() {
        for (Car3D c : cars3D) {
            Vehicle v = c.getVehicle();
            double angle = 0;
            Edge edge = v.getCurrentEdge();
            if (edge != null) {
                double dx = edge.getDestination().getX() - edge.getSource().getX();
                double dy = edge.getDestination().getY() - edge.getSource().getY();
                angle = Math.toDegrees(Math.atan2(dy, dx));
            }
            c.setPosition(v.getX(), CAR_Y, v.getY(), angle);
        }
    }

    public SimulationEngine getSimulationEngine() { return simulationEngine; }
}
