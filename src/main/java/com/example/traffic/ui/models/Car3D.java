package com.example.traffic.ui.models;

import com.example.traffic.model.Vehicle;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.transform.Rotate;

import java.util.Arrays;
import java.util.List;

/**
 * Représentation 3D d'une voiture (normale ou urgence).
 * Supporte l'animation de sirène pour les véhicules d'urgence.
 */
public class Car3D {
    private final Vehicle vehicle;
    private final Box body;
    private final Box cabin;
    private final Box windshield;
    private final Box headlightL;
    private final Box headlightR;
    private final Box taillightL; // ou sirenL pour urgence
    private final Box taillightR; // ou sirenR pour urgence

    private boolean isEmergency = false;
    private boolean sirenFlashState = false;
    private double sirenTimer = 0;

    public Car3D(Vehicle vehicle, Box body, Box cabin, Box windshield,
                 Box headlightL, Box headlightR, Box taillightL, Box taillightR) {
        this.vehicle = vehicle;
        this.body = body;
        this.cabin = cabin;
        this.windshield = windshield;
        this.headlightL = headlightL;
        this.headlightR = headlightR;
        this.taillightL = taillightL;
        this.taillightR = taillightR;

        // Toutes les pièces tournent autour de l'axe Y (vertical)
        for (Box part : getAllParts()) {
            part.setRotationAxis(Rotate.Y_AXIS);
        }
    }

    public Vehicle getVehicle() { return vehicle; }
    public Box getBody() { return body; }

    public void setEmergency(boolean emergency) {
        this.isEmergency = emergency;
    }

    public boolean isEmergency() { return isEmergency; }

    private Box[] getAllParts() {
        return new Box[]{body, cabin, windshield, headlightL, headlightR, taillightL, taillightR};
    }

    /** Liste des pièces pour suppression de la scène */
    public List<Box> getAllPartsAsList() {
        return Arrays.asList(getAllParts());
    }

    /**
     * Met à jour la sirène (flash rouge/bleu alterné).
     * Appelé chaque frame pour les véhicules d'urgence.
     */
    public void updateSiren(double deltaTime) {
        if (!isEmergency) return;

        sirenTimer += deltaTime;
        if (sirenTimer >= 0.15) { // Flash toutes les 0.15s
            sirenTimer = 0;
            sirenFlashState = !sirenFlashState;

            if (sirenFlashState) {
                taillightL.setMaterial(new PhongMaterial(Color.RED));
                taillightR.setMaterial(new PhongMaterial(Color.rgb(0, 0, 40)));
            } else {
                taillightL.setMaterial(new PhongMaterial(Color.rgb(40, 0, 0)));
                taillightR.setMaterial(new PhongMaterial(Color.BLUE));
            }
        }
    }

    /**
     * Place toutes les pièces de la voiture à la bonne position et orientation.
     * angle=0 → face +X (Est), angle=90 → face +Z (Sud)
     */
    public void setPosition(double cx, double y, double cz, double angleDeg) {
        double rad = Math.toRadians(angleDeg);
        double cos = Math.cos(rad);
        double sin = Math.sin(rad);

        // Châssis
        body.setTranslateX(cx);
        body.setTranslateY(y);
        body.setTranslateZ(cz);
        body.setRotate(angleDeg);

        // Habitacle (au-dessus du châssis)
        cabin.setTranslateX(cx);
        cabin.setTranslateY(y - 11);
        cabin.setTranslateZ(cz);
        cabin.setRotate(angleDeg);

        // Pare-brise (devant la cabine)
        double frontDist = 14;
        windshield.setTranslateX(cx + frontDist * cos);
        windshield.setTranslateY(y - 11);
        windshield.setTranslateZ(cz + frontDist * sin);
        windshield.setRotate(angleDeg);

        // Phares avant
        double headDist = 23;
        double side = 8;
        double hx = cx + headDist * cos;
        double hz = cz + headDist * sin;

        headlightL.setTranslateX(hx - side * sin);
        headlightL.setTranslateY(y);
        headlightL.setTranslateZ(hz + side * cos);
        headlightL.setRotate(angleDeg);

        headlightR.setTranslateX(hx + side * sin);
        headlightR.setTranslateY(y);
        headlightR.setTranslateZ(hz - side * cos);
        headlightR.setRotate(angleDeg);

        if (isEmergency) {
            // Gyrophares sur le toit
            taillightL.setTranslateX(cx - 10 * sin);
            taillightL.setTranslateY(y - 22);
            taillightL.setTranslateZ(cz + 10 * cos);
            taillightL.setRotate(angleDeg);

            taillightR.setTranslateX(cx + 10 * sin);
            taillightR.setTranslateY(y - 22);
            taillightR.setTranslateZ(cz - 10 * cos);
            taillightR.setRotate(angleDeg);
        } else {
            // Feux arrière
            double tailDist = 23;
            double tx = cx - tailDist * cos;
            double tz = cz - tailDist * sin;

            taillightL.setTranslateX(tx - side * sin);
            taillightL.setTranslateY(y);
            taillightL.setTranslateZ(tz + side * cos);
            taillightL.setRotate(angleDeg);

            taillightR.setTranslateX(tx + side * sin);
            taillightR.setTranslateY(y);
            taillightR.setTranslateZ(tz - side * cos);
            taillightR.setRotate(angleDeg);
        }
    }
}
