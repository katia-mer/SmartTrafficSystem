package com.example.traffic.simulation;

import com.example.traffic.graph.Node;
import com.example.traffic.model.Intersection;
import com.example.traffic.model.Vehicle;

import java.util.ArrayList;
import java.util.List;

public class SimulationEngine {

    private final List<Vehicle> vehicles = new ArrayList<>();
    private final List<Intersection> intersections = new ArrayList<>();

    // ajouter véhicule
    public void addVehicle(Vehicle v) {
        vehicles.add(v);
    }

    // ajouter intersection avec feu
    public void addIntersection(Intersection intersection) {
        intersections.add(intersection);
    }
    public List<Intersection> getIntersections() {
        return intersections;
    }

    // mise à jour simulation
    public void update(double deltaTime) {

        // mettre à jour les feux rouges / verts
        for (Intersection intersection : intersections) {
            intersection.getTrafficLight().update(deltaTime);
        }

        for (Vehicle v : vehicles) {

            if (v.getCurrentEdge() == null) {
                continue;
            }

            Intersection destinationIntersection =
                    findIntersectionByNode(v.getCurrentEdge().getDestination());

            boolean feuRougeDevant = destinationIntersection != null
                    && !destinationIntersection.getTrafficLight().isGreen();

            double progress = v.getProgress();

            // calculer la prochaine position avant de décider si la voiture doit s'arrêter
            double nextProgress = progress + (v.getSpeed() * deltaTime) / 100.0;

            // Si le feu est rouge et que la voiture va atteindre la zone d'arrêt,
            // elle s'arrête à 0.90 avant l'intersection.
            if (feuRougeDevant && progress < 1.0 && nextProgress >= 0.90) {
                v.setStopped(true);
                v.setProgress(0.90);
                continue;
            }

            // Si le feu est vert, la voiture avance.
            v.setStopped(false);

            progress = nextProgress;

            // si fin de route
            if (progress >= 1.0) {
                progress = 1.0;
                v.setCurrentNode(v.getCurrentEdge().getDestination());
            }

            v.setProgress(progress);
        }
    }

    private Intersection findIntersectionByNode(Node node) {
        for (Intersection intersection : intersections) {
            if (intersection.getNode().equals(node)) {
                return intersection;
            }
        }
        return null;
    }

    public List<Vehicle> getVehicles() {
        return vehicles;
    }
}
