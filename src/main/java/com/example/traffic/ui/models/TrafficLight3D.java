package com.example.traffic.ui.models;

import com.example.traffic.model.TrafficLight;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Sphere;

public class TrafficLight3D {
    private final TrafficLight trafficLight;
    private final Sphere redLight;
    private final Sphere yellowLight;
    private final Sphere greenLight;

    public TrafficLight3D(TrafficLight trafficLight, Sphere redLight, Sphere yellowLight, Sphere greenLight) {
        this.trafficLight = trafficLight;
        this.redLight = redLight;
        this.yellowLight = yellowLight;
        this.greenLight = greenLight;
    }

    public TrafficLight getTrafficLight() {
        return trafficLight;
    }

    /** Met à jour les 3 sphères selon l'état du feu logique */
    public void updateVisual() {
        if (trafficLight.isGreen()) {
            redLight.setMaterial(new PhongMaterial(Color.rgb(80, 20, 20)));
            yellowLight.setMaterial(new PhongMaterial(Color.rgb(80, 70, 0)));
            greenLight.setMaterial(new PhongMaterial(Color.LIMEGREEN));
        } else {
            redLight.setMaterial(new PhongMaterial(Color.RED));
            yellowLight.setMaterial(new PhongMaterial(Color.rgb(80, 70, 0)));
            greenLight.setMaterial(new PhongMaterial(Color.rgb(10, 60, 10)));
        }
    }
}
