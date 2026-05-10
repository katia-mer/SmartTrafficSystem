package com.example.traffic.ui.models;

import com.example.traffic.model.Vehicle;
import javafx.scene.PointLight;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.shape.Cylinder;
import javafx.scene.shape.Shape3D;
import javafx.scene.transform.Rotate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Représentation 3D détaillée d'une voiture.
 */
public class Car3D {
    private final Vehicle vehicle;
    private final Shape3D body;      // Partie centrale
    private final Shape3D hood;      // Avant
    private final Shape3D trunk;     // Arrière
    private final Shape3D cabin;
    private final Shape3D windshield;
    private final Shape3D headlightL;
    private final Shape3D headlightR;
    private final Shape3D taillightL;
    private final Shape3D taillightR;
    private final Shape3D mirrorL;
    private final Shape3D mirrorR;
    private final List<Shape3D> wheels = new ArrayList<>();
    private final PointLight headLight;

    private boolean isEmergency = false;
    private boolean sirenFlashState = false;
    private double sirenTimer = 0;
    
    private boolean turnSignalFlashState = false;
    private double turnSignalTimer = 0;

    public Car3D(Vehicle vehicle, Shape3D body, Shape3D hood, Shape3D trunk, 
                 Shape3D cabin, Shape3D windshield,
                 Shape3D headlightL, Shape3D headlightR, Shape3D taillightL, Shape3D taillightR,
                 Shape3D mirrorL, Shape3D mirrorR, List<Shape3D> wheels) {
        this.vehicle = vehicle;
        this.body = body;
        this.hood = hood;
        this.trunk = trunk;
        this.cabin = cabin;
        this.windshield = windshield;
        this.headlightL = headlightL;
        this.headlightR = headlightR;
        this.taillightL = taillightL;
        this.taillightR = taillightR;
        this.mirrorL = mirrorL;
        this.mirrorR = mirrorR;
        if (wheels != null) this.wheels.addAll(wheels);

        for (javafx.scene.Node part : getAllPartsAsList()) {
            if (part != null) part.setRotationAxis(Rotate.Y_AXIS);
        }
        
        this.headLight = new PointLight(Color.rgb(255, 255, 200, 0.8));
        this.headLight.setLightOn(false); // Off by default
        this.headLight.setLinearAttenuation(0.005); // Portée limitée
    }

    public Vehicle getVehicle() { return vehicle; }
    public Shape3D getBody() { return body; }

    public void setNightMode(boolean night) {
        headLight.setLightOn(night);
        if (night) {
            headlightL.setMaterial(new PhongMaterial(Color.WHITE));
            headlightR.setMaterial(new PhongMaterial(Color.WHITE));
            if (!isEmergency) {
                taillightL.setMaterial(new PhongMaterial(Color.RED));
                taillightR.setMaterial(new PhongMaterial(Color.RED));
            }
        } else {
            headlightL.setMaterial(new PhongMaterial(Color.WHITESMOKE));
            headlightR.setMaterial(new PhongMaterial(Color.WHITESMOKE));
            if (!isEmergency) {
                taillightL.setMaterial(new PhongMaterial(Color.rgb(60, 0, 0)));
                taillightR.setMaterial(new PhongMaterial(Color.rgb(60, 0, 0)));
            }
        }
    }

    public void setEmergency(boolean emergency) {
        this.isEmergency = emergency;
    }

    public boolean isEmergency() { return isEmergency; }

    public List<javafx.scene.Node> getAllPartsAsList() {
        List<javafx.scene.Node> parts = new ArrayList<>(Arrays.asList(body, hood, trunk, cabin, windshield, headlightL, headlightR, taillightL, taillightR, mirrorL, mirrorR, headLight));
        parts.addAll(wheels);
        parts.removeIf(p -> p == null);
        return parts;
    }

    public void updateSiren(double deltaTime) {
        if (!isEmergency) return;
        sirenTimer += deltaTime;
        if (sirenTimer >= 0.15) {
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

    public void updateTurnSignals(double deltaTime) {
        Vehicle.TurnSignal signal = vehicle.getTurnSignal();
        if (signal == Vehicle.TurnSignal.NONE) {
            // Reset to normal state if needed (handled in update visuals)
            return;
        }

        turnSignalTimer += deltaTime;
        if (turnSignalTimer >= 0.4) { // Clignotement standard (~1.25Hz)
            turnSignalTimer = 0;
            turnSignalFlashState = !turnSignalFlashState;
        }

        Color orange = Color.rgb(255, 160, 0);
        PhongMaterial orangeMat = new PhongMaterial(orange);
        orangeMat.setSelfIlluminationMap(null); // Simple color

        if (turnSignalFlashState) {
            if (signal == Vehicle.TurnSignal.LEFT) {
                headlightL.setMaterial(orangeMat);
                taillightL.setMaterial(orangeMat);
            } else if (signal == Vehicle.TurnSignal.RIGHT) {
                headlightR.setMaterial(orangeMat);
                taillightR.setMaterial(orangeMat);
            }
        } else {
            // Reset to default (will be overwritten by setNightMode if called, 
            // but we need to restore original colors here too)
            restoreLightColors();
        }
    }

    private void restoreLightColors() {
        boolean night = headLight.isLightOn();
        if (night) {
            headlightL.setMaterial(new PhongMaterial(Color.WHITE));
            headlightR.setMaterial(new PhongMaterial(Color.WHITE));
            if (!isEmergency) {
                taillightL.setMaterial(new PhongMaterial(Color.RED));
                taillightR.setMaterial(new PhongMaterial(Color.RED));
            }
        } else {
            headlightL.setMaterial(new PhongMaterial(Color.WHITESMOKE));
            headlightR.setMaterial(new PhongMaterial(Color.WHITESMOKE));
            if (!isEmergency) {
                taillightL.setMaterial(new PhongMaterial(Color.rgb(60, 0, 0)));
                taillightR.setMaterial(new PhongMaterial(Color.rgb(60, 0, 0)));
            }
        }
    }

    public void setPosition(double cx, double y, double cz, double angleDeg) {
        double rad = Math.toRadians(angleDeg);
        double cos = Math.cos(rad);
        double sin = Math.sin(rad);

        // Châssis Central
        setPart(body, cx, y, cz, angleDeg);

        // Capot (Hood) - Devant
        setPart(hood, cx + 18 * cos, y + 2, cz + 18 * sin, angleDeg);

        // Coffre (Trunk) - Derrière
        setPart(trunk, cx - 18 * cos, y + 2, cz - 18 * sin, angleDeg);

        // Habitacle
        setPart(cabin, cx - 2 * cos, y - 10, cz - 2 * sin, angleDeg);

        // Pare-brise
        setPart(windshield, cx + 10 * cos, y - 10, cz + 10 * sin, angleDeg);

        // Phares avant (ajustés pour être sur le capot)
        double headDist = 23;
        double side = 7;
        setPart(headlightL, cx + headDist * cos - side * sin, y + 2, cz + headDist * sin + side * cos, angleDeg);
        setPart(headlightR, cx + headDist * cos + side * sin, y + 2, cz + headDist * sin - side * cos, angleDeg);

        // Lumière projetée (plus proche du sol et du pare-chocs)
        if (headLight != null) {
            headLight.setTranslateX(cx + (headDist + 5) * cos);
            headLight.setTranslateY(y + 8);
            headLight.setTranslateZ(cz + (headDist + 5) * sin);
        }

        // Rétroviseurs
        double mirDist = 12;
        double mirSide = 15;
        setPart(mirrorL, cx + mirDist * cos - mirSide * sin, y - 6, cz + mirDist * sin + mirSide * cos, angleDeg);
        setPart(mirrorR, cx + mirDist * cos + mirSide * sin, y - 6, cz + mirDist * sin - mirSide * cos, angleDeg);

        if (isEmergency) {
            // Gyrophares
            setPart(taillightL, cx - 5 * sin, y - 22, cz + 5 * cos, angleDeg);
            setPart(taillightR, cx + 5 * sin, y - 22, cz - 5 * cos, angleDeg);
        } else {
            // Feux arrière
            double tailDist = 26;
            setPart(taillightL, cx - tailDist * cos - side * sin, y + 2, cz - tailDist * sin + side * cos, angleDeg);
            setPart(taillightR, cx - tailDist * cos + side * sin, y + 2, cz - tailDist * sin - side * cos, angleDeg);
        }

        // Roues
        if (wheels.size() >= 4) {
            double wDistF = 18;
            double wDistR = 16;
            double wSide = 14;
            double wY = y + 8;
            setPart(wheels.get(0), cx + wDistF * cos - wSide * sin, wY, cz + wDistF * sin + wSide * cos, angleDeg);
            setPart(wheels.get(1), cx + wDistF * cos + wSide * sin, wY, cz + wDistF * sin - wSide * cos, angleDeg);
            setPart(wheels.get(2), cx - wDistR * cos - wSide * sin, wY, cz - wDistR * sin + wSide * cos, angleDeg);
            setPart(wheels.get(3), cx - wDistR * cos + wSide * sin, wY, cz - wDistR * sin - wSide * cos, angleDeg);
        }
    }

    private void setPart(Shape3D part, double x, double y, double z, double angle) {
        if (part == null) return;
        part.setTranslateX(x);
        part.setTranslateY(y);
        part.setTranslateZ(z);
        part.setRotate(angle);
    }
}
