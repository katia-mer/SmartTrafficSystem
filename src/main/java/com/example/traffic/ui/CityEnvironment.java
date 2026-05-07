package com.example.traffic.ui;

import com.example.traffic.ui.models.Car3D;
import com.example.traffic.ui.models.TrafficLight3D;
import javafx.scene.AmbientLight;
import javafx.scene.Group;
import javafx.scene.PointLight;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.shape.Cylinder;
import javafx.scene.shape.Sphere;

public class CityEnvironment extends Group {

    private static final double ROAD_Y = 55;

    public CityEnvironment() {
        addLights();
        addGround();
        addSidewalks();
        addRoads();
        addRoadMarks();
        addBuildings();
        addTrees();
        addLampposts();
    }

    // ═══════════════════════════════════════════════════════
    //  ÉCLAIRAGE
    // ═══════════════════════════════════════════════════════

    private void addLights() {
        AmbientLight ambient = new AmbientLight(Color.color(0.35, 0.35, 0.42));
        this.getChildren().add(ambient);

        // Lumière principale (soleil)
        PointLight sun = new PointLight(Color.color(1.0, 0.95, 0.85));
        sun.setTranslateX(-300);
        sun.setTranslateY(-800);
        sun.setTranslateZ(-400);
        this.getChildren().add(sun);

        // Lumière d'appoint (fill light)
        PointLight fill = new PointLight(Color.color(0.4, 0.45, 0.6));
        fill.setTranslateX(400);
        fill.setTranslateY(-500);
        fill.setTranslateZ(300);
        this.getChildren().add(fill);
    }

    // ═══════════════════════════════════════════════════════
    //  SOL & TROTTOIRS
    // ═══════════════════════════════════════════════════════

    private void addGround() {
        Box ground = new Box(1200, 10, 1200);
        ground.setTranslateY(92);
        ground.setMaterial(new PhongMaterial(Color.rgb(30, 105, 45)));
        this.getChildren().add(ground);
    }

    private void addSidewalks() {
        Color sidewalkColor = Color.rgb(160, 160, 155);
        // Trottoirs le long de la route horizontale
        addBox(0, 60, -80, 950, 6, 30, sidewalkColor);
        addBox(0, 60, 80, 950, 6, 30, sidewalkColor);
        // Trottoirs le long de la route verticale
        addBox(-80, 60, 0, 30, 6, 950, sidewalkColor);
        addBox(80, 60, 0, 30, 6, 950, sidewalkColor);
    }

    // ═══════════════════════════════════════════════════════
    //  ROUTES
    // ═══════════════════════════════════════════════════════

    private void addRoads() {
        Color asphalt = Color.rgb(38, 38, 42);
        addBox(0, ROAD_Y, 0, 950, 12, 120, asphalt);
        addBox(0, ROAD_Y, 0, 120, 12, 950, asphalt);

        // Bordures de route
        Color curb = Color.rgb(130, 130, 125);
        addBox(0, 50, -65, 950, 5, 8, curb);
        addBox(0, 50, 65, 950, 5, 8, curb);
        addBox(-65, 50, 0, 8, 5, 950, curb);
        addBox(65, 50, 0, 8, 5, 950, curb);
    }

    private void addRoadMarks() {
        // Lignes centrales (pointillés jaunes)
        Color lineColor = Color.rgb(230, 200, 50);
        for (int i = -4; i <= 4; i++) {
            if (Math.abs(i) <= 0) continue; // pas dans l'intersection
            addBox(i * 85, 47, 0, 40, 2, 4, lineColor);
            addBox(0, 47, i * 85, 4, 2, 40, lineColor);
        }

        // Lignes d'arrêt (blanches)
        addBox(-80, 44, 30, 6, 3, 50, Color.WHITE);
        addBox(80, 44, -30, 6, 3, 50, Color.WHITE);
        addBox(-30, 44, -80, 50, 3, 6, Color.WHITE);
        addBox(30, 44, 80, 50, 3, 6, Color.WHITE);

        // Passages piétons
        addCrosswalk(-120, 0, true);
        addCrosswalk(120, 0, true);
        addCrosswalk(0, -120, false);
        addCrosswalk(0, 120, false);
    }

    private void addCrosswalk(double x, double z, boolean horizontal) {
        for (int i = -3; i <= 3; i++) {
            if (horizontal) {
                addBox(x, 46, z + i * 14, 8, 2, 8, Color.WHITE);
            } else {
                addBox(x + i * 14, 46, z, 8, 2, 8, Color.WHITE);
            }
        }
    }

    // ═══════════════════════════════════════════════════════
    //  FEUX TRICOLORES (3 sphères)
    // ═══════════════════════════════════════════════════════

    public TrafficLight3D addTrafficLight(com.example.traffic.model.TrafficLight logicLight, double x, double z) {
        // Poteau
        Cylinder pole = new Cylinder(3, 110);
        pole.setTranslateX(x);
        pole.setTranslateY(5);
        pole.setTranslateZ(z);
        pole.setMaterial(new PhongMaterial(Color.rgb(50, 50, 50)));

        // Boîtier
        Box box = new Box(18, 55, 14);
        box.setTranslateX(x);
        box.setTranslateY(-55);
        box.setTranslateZ(z);
        box.setMaterial(new PhongMaterial(Color.rgb(25, 25, 25)));

        // Sphère ROUGE (en haut)
        Sphere redLight = new Sphere(6);
        redLight.setTranslateX(x);
        redLight.setTranslateY(-72);
        redLight.setTranslateZ(z - 8);
        redLight.setMaterial(new PhongMaterial(Color.DARKRED));

        // Sphère JAUNE (au milieu)
        Sphere yellowLight = new Sphere(6);
        yellowLight.setTranslateX(x);
        yellowLight.setTranslateY(-55);
        yellowLight.setTranslateZ(z - 8);
        yellowLight.setMaterial(new PhongMaterial(Color.rgb(80, 70, 0)));

        // Sphère VERTE (en bas)
        Sphere greenLight = new Sphere(6);
        greenLight.setTranslateX(x);
        greenLight.setTranslateY(-38);
        greenLight.setTranslateZ(z - 8);
        greenLight.setMaterial(new PhongMaterial(Color.DARKGREEN));

        this.getChildren().addAll(pole, box, redLight, yellowLight, greenLight);

        return new TrafficLight3D(logicLight, redLight, yellowLight, greenLight);
    }

    // ═══════════════════════════════════════════════════════
    //  VOITURES MODERNES
    // ═══════════════════════════════════════════════════════

    public Car3D addCar(com.example.traffic.model.Vehicle vehicle, Color bodyColor, Color topColor) {
        // Châssis principal (plus bas et large)
        Box body = new Box(48, 14, 26);
        body.setMaterial(new PhongMaterial(bodyColor));

        // Habitacle (toit arrondi via box plus petit)
        Box cabin = new Box(26, 12, 22);
        cabin.setMaterial(new PhongMaterial(topColor));

        // Pare-brise (vitre teintée)
        Box windshield = new Box(2, 10, 18);
        windshield.setMaterial(new PhongMaterial(Color.rgb(40, 60, 90, 0.7)));

        // Phares avant (gauche et droit)
        Box headlightL = new Box(3, 5, 5);
        headlightL.setMaterial(new PhongMaterial(Color.LIGHTYELLOW));
        Box headlightR = new Box(3, 5, 5);
        headlightR.setMaterial(new PhongMaterial(Color.LIGHTYELLOW));

        // Feux arrière (rouges)
        Box taillightL = new Box(3, 4, 5);
        taillightL.setMaterial(new PhongMaterial(Color.DARKRED));
        Box taillightR = new Box(3, 4, 5);
        taillightR.setMaterial(new PhongMaterial(Color.DARKRED));

        Car3D car = new Car3D(vehicle, body, cabin, windshield, headlightL, headlightR, taillightL, taillightR);
        this.getChildren().addAll(body, cabin, windshield, headlightL, headlightR, taillightL, taillightR);

        return car;
    }

    // ═══════════════════════════════════════════════════════
    //  BÂTIMENTS
    // ═══════════════════════════════════════════════════════

    private void addBuildings() {
        // Quadrant Nord-Ouest
        addModernBuilding(-350, -320, 90, 200, 80, Color.rgb(55, 65, 85));
        addModernBuilding(-200, -380, 70, 140, 70, Color.rgb(120, 50, 50));

        // Quadrant Nord-Est
        addModernBuilding(300, -340, 100, 240, 90, Color.rgb(40, 90, 100));
        addModernBuilding(430, -200, 70, 160, 70, Color.rgb(100, 70, 40));

        // Quadrant Sud-Ouest
        addModernBuilding(-380, 310, 100, 180, 90, Color.rgb(60, 45, 90));
        addModernBuilding(-200, 370, 70, 120, 65, Color.rgb(75, 75, 80));

        // Quadrant Sud-Est
        addModernBuilding(280, 320, 110, 200, 100, Color.rgb(90, 70, 30));
        addModernBuilding(430, 210, 70, 150, 70, Color.rgb(90, 30, 80));
    }

    private void addModernBuilding(double x, double z, double w, double h, double d, Color baseColor) {
        // Corps principal
        Box building = new Box(w, h, d);
        building.setTranslateX(x);
        building.setTranslateY(92 - h / 2);
        building.setTranslateZ(z);
        building.setMaterial(new PhongMaterial(baseColor));
        this.getChildren().add(building);

        // Toit
        Box roof = new Box(w + 6, 5, d + 6);
        roof.setTranslateX(x);
        roof.setTranslateY(92 - h - 2);
        roof.setTranslateZ(z);
        roof.setMaterial(new PhongMaterial(baseColor.darker()));
        this.getChildren().add(roof);

        // Fenêtres (grille sur la façade, strictement à l'intérieur du bâtiment)
        double buildingTop = 92 - h;
        double buildingBottom = 92;
        double margin = 15;
        double windowH = 14;
        double windowW = 10;
        double usableHeight = h - 2 * margin;
        double usableWidth = w - 2 * margin;

        int rows = Math.max(1, (int) (usableHeight / 35));
        int cols = Math.max(1, (int) (usableWidth / 22));

        Color windowColor = Color.rgb(255, 230, 140, 0.9);
        Color windowOffColor = Color.rgb(80, 100, 130, 0.8);

        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                double wx = x - usableWidth / 2 + col * usableWidth / Math.max(1, cols - 1);
                double wy = buildingTop + margin + row * (usableHeight / Math.max(1, rows));
                boolean lightOn = Math.random() > 0.35;

                // Vérifier que la fenêtre reste dans le bâtiment
                if (wy - windowH / 2 < buildingTop + 5 || wy + windowH / 2 > buildingBottom - 5) continue;

                Box window = new Box(windowW, windowH, 2);
                window.setTranslateX(wx);
                window.setTranslateY(wy);
                window.setTranslateZ(z - d / 2 - 1.2);
                window.setMaterial(new PhongMaterial(lightOn ? windowColor : windowOffColor));
                this.getChildren().add(window);
            }
        }
    }

    // ═══════════════════════════════════════════════════════
    //  VÉGÉTATION
    // ═══════════════════════════════════════════════════════

    private void addTrees() {
        // Le long de la route horizontale
        addTree(-350, -120);
        addTree(-250, -120);
        addTree(250, -120);
        addTree(350, -120);
        addTree(-350, 120);
        addTree(-250, 120);
        addTree(250, 120);
        addTree(350, 120);

        // Le long de la route verticale
        addTree(-120, -350);
        addTree(-120, -250);
        addTree(120, -350);
        addTree(120, -250);
        addTree(-120, 250);
        addTree(-120, 350);
        addTree(120, 250);
        addTree(120, 350);
    }

    private void addTree(double x, double z) {
        // Tronc
        Cylinder trunk = new Cylinder(4, 35);
        trunk.setTranslateX(x);
        trunk.setTranslateY(60);
        trunk.setTranslateZ(z);
        trunk.setMaterial(new PhongMaterial(Color.rgb(90, 60, 30)));

        // Feuillage (3 couches)
        Sphere leaves1 = new Sphere(18);
        leaves1.setTranslateX(x);
        leaves1.setTranslateY(32);
        leaves1.setTranslateZ(z);
        leaves1.setMaterial(new PhongMaterial(Color.rgb(30, 120, 50)));

        Sphere leaves2 = new Sphere(14);
        leaves2.setTranslateX(x);
        leaves2.setTranslateY(18);
        leaves2.setTranslateZ(z);
        leaves2.setMaterial(new PhongMaterial(Color.rgb(35, 135, 55)));

        Sphere leaves3 = new Sphere(9);
        leaves3.setTranslateX(x);
        leaves3.setTranslateY(8);
        leaves3.setTranslateZ(z);
        leaves3.setMaterial(new PhongMaterial(Color.rgb(40, 150, 60)));

        this.getChildren().addAll(trunk, leaves1, leaves2, leaves3);
    }

    // ═══════════════════════════════════════════════════════
    //  LAMPADAIRES
    // ═══════════════════════════════════════════════════════

    private void addLampposts() {
        addLamppost(-150, -90);
        addLamppost(150, -90);
        addLamppost(-150, 90);
        addLamppost(150, 90);

        addLamppost(-90, -150);
        addLamppost(90, -150);
        addLamppost(-90, 150);
        addLamppost(90, 150);
    }

    private void addLamppost(double x, double z) {
        Cylinder pole = new Cylinder(2.5, 80);
        pole.setTranslateX(x);
        pole.setTranslateY(20);
        pole.setTranslateZ(z);
        pole.setMaterial(new PhongMaterial(Color.rgb(60, 60, 65)));

        // Lampe
        Sphere lamp = new Sphere(5);
        lamp.setTranslateX(x);
        lamp.setTranslateY(-25);
        lamp.setTranslateZ(z);
        lamp.setMaterial(new PhongMaterial(Color.rgb(255, 240, 180)));

        // Lumière ponctuelle
        PointLight light = new PointLight(Color.rgb(255, 230, 170, 0.3));
        light.setTranslateX(x);
        light.setTranslateY(-20);
        light.setTranslateZ(z);

        this.getChildren().addAll(pole, lamp, light);
    }

    // ═══════════════════════════════════════════════════════
    //  UTILITAIRE
    // ═══════════════════════════════════════════════════════

    private void addBox(double x, double y, double z,
                        double width, double height, double depth,
                        Color color) {
        Box box = new Box(width, height, depth);
        box.setTranslateX(x);
        box.setTranslateY(y);
        box.setTranslateZ(z);
        box.setMaterial(new PhongMaterial(color));
        this.getChildren().add(box);
    }
}
