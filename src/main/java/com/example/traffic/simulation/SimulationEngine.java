package com.example.traffic.simulation;

import com.example.traffic.model.Vehicle;
import java.util.ArrayList;
import java.util.List;

public class SimulationEngine {

    private final List<Vehicle> vehicles = new ArrayList<>();

    // ajouter véhicule
    public void addVehicle(Vehicle v) {
        vehicles.add(v);
    }

    // mise à jour simulation
    public void update(double deltaTime) {

        for (Vehicle v : vehicles) {

            // si arrêté → ne bouge pas
            if (v.isStopped()) continue;

            double progress = v.getProgress();

            // avancer selon vitesse
            progress += (v.getSpeed() * deltaTime) / 100.0;

            // si fin de route
            if (progress >= 1.0) {
                progress = 0.0;
                // TODO : changer d’arête plus tard
            }

            v.setProgress(progress);
        }
    }

    public List<Vehicle> getVehicles() {
        return vehicles;
    }
}