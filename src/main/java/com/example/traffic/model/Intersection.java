package com.example.traffic.model;

import com.example.traffic.graph.Node;

public class Intersection {

    private final Node node;
    private final TrafficLight trafficLight;

    public Intersection(Node node) {
        this.node = node;
        this.trafficLight = new TrafficLight();
    }

    public Node getNode() {
        return node;
    }

    public TrafficLight getTrafficLight() {
        return trafficLight;
    }
}