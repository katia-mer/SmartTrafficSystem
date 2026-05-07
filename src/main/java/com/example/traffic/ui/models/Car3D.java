package com.example.traffic.ui.models;

import com.example.traffic.model.Vehicle;
import javafx.scene.shape.Box;
import javafx.scene.transform.Rotate;

/**
 * Représentation 3D d'une voiture moderne.
 */
public class Car3D {
    private final Vehicle vehicle;
    private final Box body;
    private final Box cabin;
    private final Box windshield;
    private final Box headlightL;
    private final Box headlightR;
    private final Box taillightL;
    private final Box taillightR;

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

    private Box[] getAllParts() {
        return new Box[]{body, cabin, windshield, headlightL, headlightR, taillightL, taillightR};
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
