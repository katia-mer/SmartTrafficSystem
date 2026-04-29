package com.example.traffic;

import com.example.traffic.graph.*;
import com.example.traffic.model.*;
import com.example.traffic.simulation.*;

public class MainTest {

    public static void main(String[] args) {

        // créer graphe
        Node n1 = new Node("A", 0, 0);
        Node n2 = new Node("B", 100, 0);

        Edge e = new Edge(n1, n2, 50);

        // créer intersection au point B avec un feu
        Intersection intersectionB = new Intersection(n2);

        // créer véhicule
        Vehicle v = new Vehicle("V1", n1, 10);
        v.setCurrentEdge(e);

        // simulation
        SimulationEngine sim = new SimulationEngine();
        sim.addVehicle(v);
        sim.addIntersection(intersectionB);

        // boucle simple
        for (int i = 0; i < 15; i++) {
            sim.update(1.0);

            System.out.println(
                    "Feu B = " + intersectionB.getTrafficLight().getState()
                            + " | " + v
            );
        }
    }
}