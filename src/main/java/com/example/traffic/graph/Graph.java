package com.example.traffic.graph;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Graphe orienté représentant le réseau routier.
 * Chaque Node est une intersection, chaque Edge est une route.
 */
public class Graph {

    private final Map<String, Node>       nodes     = new HashMap<>();
    private final List<Edge>              edges     = new ArrayList<>();
    private final Map<String, List<Edge>> adjacency = new HashMap<>();

    // ── Ajouter un nœud ───────────────────────────────────
    public void addNode(Node node) {
        nodes.put(node.getId(), node);
        adjacency.putIfAbsent(node.getId(), new ArrayList<>());
    }

    // ── Ajouter une arête orientée (sens unique) ──────────
    public void addEdge(Edge edge) {
        edges.add(edge);
        adjacency
                .computeIfAbsent(edge.getSource().getId(), k -> new ArrayList<>())
                .add(edge);
    }

    // ── Ajouter une route dans les deux sens ──────────────
    public void addBidirectionalEdge(Node a, Node b, int speedLimit) {
        addEdge(new Edge(a, b, speedLimit));
        addEdge(new Edge(b, a, speedLimit));
    }

    // ── Voisins d'un nœud (arêtes sortantes) ─────────────
    public List<Edge> getNeighbors(String nodeId) {
        return adjacency.getOrDefault(nodeId, Collections.emptyList());
    }

    // ── Récupérer un nœud par son id ─────────────────────
    public Node getNode(String id) {
        return nodes.get(id);
    }

    // ── Accesseurs globaux ────────────────────────────────
    public List<Node> getAllNodes() {
        return new ArrayList<>(nodes.values());
    }

    public List<Edge> getAllEdges() {
        return Collections.unmodifiableList(edges);
    }

    public int getNodeCount() { return nodes.size(); }
    public int getEdgeCount() { return edges.size(); }

    @Override
    public String toString() {
        return "Graph[" + getNodeCount() + " nœuds, "
                + getEdgeCount() + " arêtes]";
    }
}