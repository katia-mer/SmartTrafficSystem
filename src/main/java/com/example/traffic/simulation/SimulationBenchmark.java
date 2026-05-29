package com.example.traffic.simulation;

import com.example.traffic.ai.TrafficState;
import com.example.traffic.graph.Edge;
import com.example.traffic.graph.Graph;
import com.example.traffic.graph.Node;
import com.example.traffic.model.Intersection;
import com.example.traffic.model.Vehicle;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Exécute un scénario identique en mode classique et en mode IA.
 * Les résultats sont utilisés uniquement pour afficher des graphes comparatifs.
 */
public class SimulationBenchmark {

    private static final double DT = 0.1;
    private static final double DURATION_SECONDS = 180.0;
    private static final double SAMPLE_INTERVAL_SECONDS = 10.0;

    public static ComparisonResult runDefault() {
        long runSeed = System.currentTimeMillis();
        
        // On choisit les conditions aléatoirement une seule fois pour les deux runs
        int levelPick = (int)(runSeed % 3);
        SimulationEngine.TrafficLevel level = SimulationEngine.TrafficLevel.values()[levelPick];
        boolean rush = runSeed % 2 == 0;
        
        List<DataPoint> classic = runScenario(false, runSeed, level, rush);
        List<DataPoint> ai = runScenario(true, runSeed, level, rush); // Même seed pour comparer le même trafic
        
        ComparisonResult res = new ComparisonResult(classic, ai);
        res.trafficLevel = level.name();
        res.rushHour = rush;
        return res;
    }

    public static List<DataPoint> runScenario(boolean aiMode, long seed, SimulationEngine.TrafficLevel level, boolean rushHour, double durationSeconds) {
        TrafficState.resetHistory();

        SimulationEngine engine = new SimulationEngine(seed);
        buildNetwork(engine);
        
        engine.setTrafficLevel(level);
        engine.setRushHour(rushHour);
        engine.setAiMode(aiMode);

        List<DataPoint> points = new ArrayList<>();
        double nextSample = 0.0;

        int steps = (int) Math.round(durationSeconds / DT);
        for (int i = 0; i <= steps; i++) {
            double time = i * DT;
            if (time >= nextSample || i == steps) {
                points.add(new DataPoint(time, averageWait(engine), engine.getVehiclesCompleted()));
                nextSample += 5.0; // Aligner sur l'intervalle de l'historique live
            }
            engine.update(DT);
        }

        return points;
    }

    private static List<DataPoint> runScenario(boolean aiMode, long seed, SimulationEngine.TrafficLevel level, boolean rushHour) {
        return runScenario(aiMode, seed, level, rushHour, DURATION_SECONDS);
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

        Node cWS = n(graph, "CWS", -40, 40);  // W -> S (Droite)
        Node cWN = n(graph, "CWN", 40, 40);   // W -> N (Gauche)
        
        Node cEN = n(graph, "CEN", 40, -40);  // E -> N (Droite)
        Node cES = n(graph, "CES", -40, -40); // E -> S (Gauche)
        
        Node cNW = n(graph, "CNW", -40, -40); // N -> W (Droite)
        Node cNE = n(graph, "CNE", -40, 40);  // N -> E (Gauche)
        
        Node cSE = n(graph, "CSE", 40, 40);   // S -> E (Droite)
        Node cSW = n(graph, "CSW", 40, -40);  // S -> W (Gauche)

        e(graph, wE, wS);
        e(graph, eE, eS);
        e(graph, nE, nS);
        e(graph, sE, sS);

        // 1. TOUT DROIT (Index 0)
        e(graph, wS, eX);
        e(graph, eS, wX);
        e(graph, nS, sX);
        e(graph, sS, nX);

        // 2. VIRAGES À DROITE (Index 1)
        e(graph, wS, cWS); e(graph, cWS, sX);
        e(graph, eS, cEN); e(graph, cEN, nX);
        e(graph, nS, cNW); e(graph, cNW, wX);
        e(graph, sS, cSE); e(graph, cSE, eX);

        // 3. VIRAGES À GAUCHE (Index 2)
        e(graph, wS, cWN); e(graph, cWN, nX);
        e(graph, eS, cES); e(graph, cES, sX);
        e(graph, nS, cNE); e(graph, cNE, eX);
        e(graph, sS, cSW); e(graph, cSW, wX);

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
        public String trafficLevel;
        public boolean rushHour;

        public ComparisonResult(List<DataPoint> classic, List<DataPoint> ai) {
            this.classic = classic;
            this.ai = ai;
        }

        public void exportToJson(String filePath) {
            exportToJson(filePath, this.trafficLevel, this.rushHour, this.classic, this.ai);
        }

        public static void exportLiveSession(String filePath, String trafficLevel, boolean rushHour, List<DataPoint> points, boolean isAi) {
            exportToJson(filePath, trafficLevel, rushHour, isAi ? null : points, isAi ? points : null);
        }

        private static void exportToJson(String filePath, String trafficLevel, boolean rushHour, List<DataPoint> classic, List<DataPoint> ai) {
            StringBuilder sb = new StringBuilder();
            sb.append("{\n");
            sb.append("  \"metadata\": {\n");
            sb.append("    \"trafficLevel\": \"").append(trafficLevel).append("\",\n");
            sb.append("    \"rushHour\": ").append(rushHour).append(",\n");
            sb.append("    \"timestamp\": \"").append(new java.util.Date()).append("\"\n");
            sb.append("  },\n");
            
            // Stats de base
            sb.append("  \"wait\": {\n");
            
            List<DataPoint> labelsSource = (classic != null) ? classic : ai;
            sb.append("    \"labels\": [");
            if (labelsSource != null) {
                for (int i = 0; i < labelsSource.size(); i++) {
                    sb.append("\"").append((int)labelsSource.get(i).timeSeconds).append("s\"");
                    if (i < labelsSource.size() - 1) sb.append(",");
                }
            }
            sb.append("],\n");
            
            sb.append("    \"datasets\": [\n");
            boolean first = true;
            if (classic != null) {
                appendDataset(sb, "Session Live (Classique)", classic, "#f59e0b");
                first = false;
            }
            if (ai != null) {
                if (!first) sb.append(",\n");
                appendDataset(sb, "Session Live (IA)", ai, "#38bdf8");
            }
            sb.append("\n    ]\n");
            sb.append("  }\n");
            sb.append("}");

            try (FileWriter writer = new FileWriter(filePath)) {
                writer.write(sb.toString());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private static void appendDataset(StringBuilder sb, String label, List<DataPoint> points, String color) {
            sb.append("      {\n");
            sb.append("        \"label\": \"").append(label).append("\",\n");
            sb.append("        \"data\": [");
            for (int i = 0; i < points.size(); i++) {
                sb.append(String.format("%.2f", points.get(i).averageWaitSeconds).replace(",", "."));
                if (i < points.size() - 1) sb.append(",");
            }
            sb.append("],\n");
            sb.append("        \"borderColor\": \"").append(color).append("\",\n");
            sb.append("        \"backgroundColor\": \"").append(color).append("1A\",\n");
            sb.append("        \"fill\": true,\n");
            sb.append("        \"tension\": 0.4\n");
            sb.append("      }");
        }
    }
}
