package com.example.traffic.simulation;

import com.example.traffic.graph.Edge;
import com.example.traffic.graph.Node;
import com.example.traffic.model.Intersection;
import com.example.traffic.model.Vehicle;

public class SimulationManualTest {

    public static void main(String[] args) {

        System.out.println("=== TEST 1 : Feu rouge -> la voiture s'arrête ===");
        testFeuRougeArret();

        System.out.println();

        System.out.println("=== TEST 2 : Feu vert -> la voiture avance ===");
        testFeuVertAvance();

        System.out.println();

        System.out.println("=== TEST 3 : Simulation complète rouge / vert ===");
        testSimulationComplete();
    }

    private static void testFeuRougeArret() {
        Node n1 = new Node("A", 0, 0);
        Node n2 = new Node("B", 100, 0);

        Edge route = new Edge(n1, n2, 50);

        Intersection intersectionB = new Intersection(n2);

        Vehicle vehicle = new Vehicle("V1", n1, 10);
        vehicle.setCurrentEdge(route);
        vehicle.setProgress(0.85);

        SimulationEngine simulation = new SimulationEngine();
        simulation.addVehicle(vehicle);
        simulation.addIntersection(intersectionB);

        simulation.update(1.0);

        System.out.println("Feu B = " + intersectionB.getTrafficLight().getState());
        System.out.println(vehicle);

        if (vehicle.isStopped() && Math.abs(vehicle.getProgress() - 0.90) < 0.001) {
            System.out.println("Résultat : OK, la voiture s'arrête au feu rouge.");
        } else {
            System.out.println("Résultat : ERREUR, la voiture ne s'arrête pas correctement.");
        }
    }

    private static void testFeuVertAvance() {
        Node n1 = new Node("A", 0, 0);
        Node n2 = new Node("B", 100, 0);

        Edge route = new Edge(n1, n2, 50);

        Intersection intersectionB = new Intersection(n2);

        // Le feu commence rouge, donc on le passe en vert.
        intersectionB.getTrafficLight().toggle();

        Vehicle vehicle = new Vehicle("V2", n1, 10);
        vehicle.setCurrentEdge(route);
        vehicle.setProgress(0.50);

        SimulationEngine simulation = new SimulationEngine();
        simulation.addVehicle(vehicle);
        simulation.addIntersection(intersectionB);

        simulation.update(1.0);

        System.out.println("Feu B = " + intersectionB.getTrafficLight().getState());
        System.out.println(vehicle);

        if (!vehicle.isStopped() && Math.abs(vehicle.getProgress() - 0.60) < 0.001) {
            System.out.println("Résultat : OK, la voiture avance au feu vert.");
        } else {
            System.out.println("Résultat : ERREUR, la voiture n'avance pas correctement.");
        }
    }

    private static void testSimulationComplete() {
        Node n1 = new Node("A", 0, 0);
        Node n2 = new Node("B", 100, 0);

        Edge route = new Edge(n1, n2, 50);

        Intersection intersectionB = new Intersection(n2);

        Vehicle vehicle = new Vehicle("V3", n1, 10);
        vehicle.setCurrentEdge(route);

        SimulationEngine simulation = new SimulationEngine();
        simulation.addVehicle(vehicle);
        simulation.addIntersection(intersectionB);

        for (int i = 1; i <= 15; i++) {
            simulation.update(1.0);

            System.out.println(
                    "Seconde " + i
                            + " | Feu B = " + intersectionB.getTrafficLight().getState()
                            + " | " + vehicle
            );
        }
    }
}