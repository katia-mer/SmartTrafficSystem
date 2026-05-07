package com.example.traffic.ui.models;

import com.example.traffic.model.TrafficLight;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Sphere;

/**
 * Représentation 3D d'un feu tricolore.
 * Supporte les 4 états : RED, GREEN, YELLOW, RED_YELLOW.
 */
public class TrafficLight3D {
    private final TrafficLight trafficLight;
    private final Sphere redLight;
    private final Sphere yellowLight;
    private final Sphere greenLight;

    // Couleurs allumées
    private static final Color RED_ON = Color.RED;
    private static final Color YELLOW_ON = Color.rgb(255, 200, 0);
    private static final Color GREEN_ON = Color.LIMEGREEN;

    // Couleurs éteintes
    private static final Color RED_OFF = Color.rgb(80, 20, 20);
    private static final Color YELLOW_OFF = Color.rgb(80, 70, 0);
    private static final Color GREEN_OFF = Color.rgb(10, 60, 10);

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
        TrafficLight.State state = trafficLight.getState();

        switch (state) {
            case GREEN:
                redLight.setMaterial(new PhongMaterial(RED_OFF));
                yellowLight.setMaterial(new PhongMaterial(YELLOW_OFF));
                greenLight.setMaterial(new PhongMaterial(GREEN_ON));
                break;

            case YELLOW:
                redLight.setMaterial(new PhongMaterial(RED_OFF));
                yellowLight.setMaterial(new PhongMaterial(YELLOW_ON));
                greenLight.setMaterial(new PhongMaterial(GREEN_OFF));
                break;

            case RED_YELLOW:
                redLight.setMaterial(new PhongMaterial(RED_ON));
                yellowLight.setMaterial(new PhongMaterial(YELLOW_ON));
                greenLight.setMaterial(new PhongMaterial(GREEN_OFF));
                break;

            case RED:
            default:
                redLight.setMaterial(new PhongMaterial(RED_ON));
                yellowLight.setMaterial(new PhongMaterial(YELLOW_OFF));
                greenLight.setMaterial(new PhongMaterial(GREEN_OFF));
                break;
        }
    }
}
