package com.example.traffic.simulation;

import com.example.traffic.ai.TrafficState;
import com.example.traffic.graph.Edge;
import com.example.traffic.graph.Graph;
import com.example.traffic.graph.Node;
import com.example.traffic.model.Intersection;
import com.example.traffic.model.Vehicle;

import java.util.ArrayList;
import java.util.List;

/**
 * Exécute un scénario identique en mode classique et en mode IA.
 * Les résultats sont utilisés uniquement pour afficher des graphes comparatifs.
 */
public class SimulationBenchmark {

    private static final long SEED = 20260528L;
    private static final double DT = 0.1;
    private static final double DURATION_SECONDS = 180.0;
    private static final double SAMPLE_INTERVAL_SECONDS = 10.0;

    public static ComparisonResult runDefault() {
        List<DataPoint> classic = runScenario(false);
        List<DataPoint> ai = runScenario(true);
        return new ComparisonResult(classic, ai);
    }

    private static List<DataPoint> runScenario(boolean aiMode) {
        TrafficState.resetHistory();

        SimulationEngine engine = new SimulationEngine(SEED);
        buildNetwork(engine);
        engine.setTrafficLevel(SimulationEngine.TrafficLevel.HIGH);
        engine.setRushHour(true);
        engine.setAiMode(aiMode);

        List<DataPoint> points = new ArrayList<>();
        double nextSample = 0.0;

        int steps = (int) Math.round(DURATION_SECONDS / DT);
        for (int i = 0; i <= steps; i++) {
            double time = i * DT;
            if (time >= nextSample || i == steps) {
                points.add(new DataPoint(time, averageWait(engine), engine.getVehiclesCompleted()));
                nextSample += SAMPLE_INTERVAL_SECONDS;
            }
            engine.update(DT);
        }

        return points;
    }

    private static double averageWait(SimulationEngine engine) {
        double total = 0.0;
        int count = 0;

        for (Vehicle vehicle : engine.getVehicles()) {
            if (!vehicle.isEmergency()) {
                total += vehicle.getWaitTime();
                count++;
            }
        }

        return count == 0 ? 0.0 : total / count;
    }

    private static void buildNetwork(SimulationEngine engine) {
        Graph graph = new Graph();
        engine.setGraph(graph);

        Node wE = n(graph, "WE", -1500, 40);
        Node eE = n(graph, "EE", 1500, -40);
        Node nE = n(graph, "NE", -40, -1500);
        Node sE = n(graph, "SE", 40, 1500);

        Node wS = n(graph, "WS", -90, 40);
        Node eS = n(graph, "ES", 90, -40);
        Node nS = n(graph, "NS", -40, -90);
        Node sS = n(graph, "SS", 40, 90);

        Node wX = n(graph, "WX", -1500, -40);
        Node eX = n(graph, "EX", 1500, 40);
        Node nX = n(graph, "NX", 40, -1500);
        Node sX = n(graph, "SX", -40, 1500);

        Node cWS = n(graph, "CWS", -40, 40);
        Node cEN = n(graph, "CEN", 40, -40);
        Node cNW = n(graph, "CNW", -40, -40);
        Node cSE = n(graph, "CSE", 40, 40);
        Node cWN = n(graph, "CWN", 40, 40);
        Node cES = n(graph, "CES", -40, -40);

        e(graph, wE, wS);
        e(graph, eE, eS);
        e(graph, nE, nS);
        e(graph, sE, sS);

        e(graph, wS, eX);
        e(graph, eS, wX);
        e(graph, nS, sX);
        e(graph, sS, nX);

        e(graph, wS, cWS);
        e(graph, cWS, sX);
        e(graph, eS, cEN);
        e(graph, cEN, nX);
        e(graph, nS, cNW);
        e(graph, cNW, wX);
        e(graph, sS, cSE);
        e(graph, cSE, eX);

        e(graph, wS, cWN);
        e(graph, cWN, nX);
        e(graph, eS, cES);
        e(graph, cES, sX);
        e(graph, nS, cWS);
        e(graph, cWS, eX);
        e(graph, sS, cEN);
        e(graph, cEN, wX);

        engine.addEntryNodeId("WE");
        engine.addEntryNodeId("EE");
        engine.addEntryNodeId("NE");
        engine.addEntryNodeId("SE");

        engine.addIntersection(new Intersection(wS));
        engine.addIntersection(new Intersection(eS));
        engine.addIntersection(new Intersection(nS));
        engine.addIntersection(new Intersection(sS));
    }

    private static Node n(Graph graph, String id, double x, double y) {
        Node node = new Node(id, x, y);
        graph.addNode(node);
        return node;
    }

    private static Edge e(Graph graph, Node src, Node dst) {
        Edge edge = new Edge(src, dst, 50);
        graph.addEdge(edge);
        return edge;
    }

    public static class DataPoint {
        public final double timeSeconds;
        public final double averageWaitSeconds;
        public final int completedVehicles;

        public DataPoint(double timeSeconds, double averageWaitSeconds, int completedVehicles) {
            this.timeSeconds = timeSeconds;
            this.averageWaitSeconds = averageWaitSeconds;
            this.completedVehicles = completedVehicles;
        }
    }

    public static class ComparisonResult {
        public final List<DataPoint> classic;
        public final List<DataPoint> ai;

        public ComparisonResult(List<DataPoint> classic, List<DataPoint> ai) {
            this.classic = classic;
            this.ai = ai;
        }
    }
}
