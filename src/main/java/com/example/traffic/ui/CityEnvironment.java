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

/**
 * Environnement 3D de la ville avec support dynamique :
 * - Ajout/suppression de voitures à la volée
 * - Véhicules d'urgence avec gyrophare
 * - Mode nuit (éclairage modifiable)
 */
public class CityEnvironment extends Group {

    private static final double ROAD_Y = 55;

    private final AmbientLight ambientLight;
    private final PointLight sunLight;
    private final PointLight fillLight;
    private final Group rainSystem = new Group();
    private Sphere sky;
    private PhongMaterial skyMat;

    // Couleurs selon le type d'urgence
    private static final Color AMBULANCE_COLOR = Color.WHITE;
    private static final Color FIRE_COLOR = Color.rgb(220, 30, 30);
    private static final Color POLICE_COLOR = Color.rgb(30, 60, 200);
    private static final Color RESCUE_COLOR = Color.rgb(255, 170, 0);

    public CityEnvironment() {
        // Éclairage (gardé comme référence pour mode nuit)
        // Éclairage plus vif pour le mode normal
        ambientLight = new AmbientLight(Color.color(0.55, 0.55, 0.62));
        this.getChildren().add(ambientLight);

        sunLight = new PointLight(Color.color(1.0, 0.95, 0.85));
        sunLight.setTranslateX(-300);
        sunLight.setTranslateY(-800);
        sunLight.setTranslateZ(-400);
        this.getChildren().add(sunLight);

        fillLight = new PointLight(Color.color(0.6, 0.65, 0.8));
        fillLight.setTranslateX(400);
        fillLight.setTranslateY(-500);
        fillLight.setTranslateZ(300);
        this.getChildren().add(fillLight);

        addGround();
        addSidewalks();
        addRoads();
        addRoadMarks();
        addBuildings();
        addTrees();
        addLampposts();
        createSkybox();
        createRainSystem();
    }

    // ═══════════════════════════════════════════════════════
    //  MODE NUIT
    // ═══════════════════════════════════════════════════════

    public void setNightMode(boolean night) {
        Color skyColor = night ? Color.rgb(10, 15, 30) : Color.rgb(135, 206, 235);
        if (night) {
            ambientLight.setColor(Color.color(0.12, 0.12, 0.18));
            sunLight.setColor(Color.color(0.3, 0.28, 0.25));
            fillLight.setColor(Color.color(0.15, 0.18, 0.25));
        } else {
            ambientLight.setColor(Color.color(0.55, 0.55, 0.62));
            sunLight.setColor(Color.color(1.0, 1.0, 1.0));
            fillLight.setColor(Color.color(0.6, 0.65, 0.8));
        }
        
        if (skyMat != null) skyMat.setDiffuseColor(skyColor);
    }

    public void setRainMode(boolean rain) {
        rainSystem.setVisible(rain);
        if (rain) {
            Color rainSky = Color.rgb(70, 80, 95);
            ambientLight.setColor(ambientLight.getColor().deriveColor(0, 0.7, 0.8, 1.0));
            sunLight.setColor(sunLight.getColor().deriveColor(0, 0.5, 0.6, 1.0));
            if (skyMat != null) skyMat.setDiffuseColor(rainSky);
        } else {
            setNightMode(false); // Reset to default
        }
    }


    private void createRainSystem() {
        rainSystem.setVisible(false);
        PhongMaterial rainMat = new PhongMaterial(Color.rgb(150, 180, 255, 0.4));
        java.util.Random rnd = new java.util.Random();
        for (int i = 0; i < 500; i++) {
            Cylinder drop = new Cylinder(0.5, 15);
            drop.setMaterial(rainMat);
            drop.setTranslateX(rnd.nextDouble() * 1200 - 600);
            drop.setTranslateY(rnd.nextDouble() * 600 - 400); // Plage de hauteur plus grande
            drop.setTranslateZ(rnd.nextDouble() * 1200 - 600);
            drop.setRotate(10); // Pluie tombant un peu de biais
            rainSystem.getChildren().add(drop);
        }
        this.getChildren().add(rainSystem);
    }

    public void updateRain(double dt) {
        if (!rainSystem.isVisible()) return;
        
        for (javafx.scene.Node node : rainSystem.getChildren()) {
            if (node instanceof Cylinder) {
                Cylinder drop = (Cylinder) node;
                double speed = 400 + Math.random() * 200; // Vitesse variée
                drop.setTranslateY(drop.getTranslateY() + speed * dt);
                
                // Si la goutte touche le sol (y > 90), on la remet en haut
                if (drop.getTranslateY() > 100) {
                    drop.setTranslateY(-400);
                }
            }
        }
    }

    private void createSkybox() {
        // Dôme céleste principal (couleur brumeuse)
        sky = new Sphere(2500);
        skyMat = new PhongMaterial(Color.rgb(210, 225, 240));
        skyMat.setSpecularColor(Color.WHITE);
        skyMat.setSpecularPower(10);
        sky.setMaterial(skyMat);
        sky.setCullFace(javafx.scene.shape.CullFace.FRONT);
        this.getChildren().add(sky);

        // Couche de brouillard lumineux (halo à l'horizon)
        Sphere horizonFog = new Sphere(2450);
        PhongMaterial fogMat = new PhongMaterial(Color.rgb(255, 255, 255, 0.4));
        horizonFog.setMaterial(fogMat);
        horizonFog.setCullFace(javafx.scene.shape.CullFace.FRONT);
        this.getChildren().add(horizonFog);
    }


    // ═══════════════════════════════════════════════════════
    //  SOL & TROTTOIRS
    // ═══════════════════════════════════════════════════════

    private void addGround() {
        Color groundColor = Color.rgb(20, 22, 28);
        addBox(0, 60, 0, 10000, 2, 10000, groundColor);
    }

    private void addSidewalks() {
        Color sidewalkColor = Color.rgb(160, 160, 155);
        // Trottoirs le long de la route horizontale (ajustés pour route 160)
        addBox(0, 60, -100, 5000, 6, 40, sidewalkColor);
        addBox(0, 60, 100, 5000, 6, 40, sidewalkColor);
        // Trottoirs le long de la route verticale
        addBox(-100, 60, 0, 40, 6, 5000, sidewalkColor);
        addBox(100, 60, 0, 40, 6, 5000, sidewalkColor);
    }

    // ═══════════════════════════════════════════════════════
    //  ROUTES
    // ═══════════════════════════════════════════════════════

    private void addRoads() {
        Color asphalt = Color.rgb(25, 26, 30);
        addBox(0, ROAD_Y, 0, 5000, 12, 160, asphalt);
        addBox(0, ROAD_Y, 0, 160, 12, 5000, asphalt);

        // Lignes néon vert fluo le long des routes
        Color neonGreen = Color.rgb(0, 255, 150);
        addBox(0, ROAD_Y - 2, -78, 5000, 1, 3, neonGreen);
        addBox(0, ROAD_Y - 2, 78, 5000, 1, 3, neonGreen);
        addBox(-78, ROAD_Y - 2, 0, 3, 1, 5000, neonGreen);
        addBox(78, ROAD_Y - 2, 0, 3, 1, 5000, neonGreen);

        // Bordures de route
        Color curb = Color.rgb(60, 60, 65);
        addBox(0, 50, -85, 5000, 5, 10, curb);
        addBox(0, 50, 85, 5000, 5, 10, curb);
        addBox(-85, 50, 0, 10, 5, 5000, curb);
        addBox(85, 50, 0, 10, 5, 5000, curb);
    }

    private void addRoadMarks() {
        // Lignes centrales (pointillés jaunes)
        Color lineColor = Color.rgb(230, 200, 50);
        for (int i = -8; i <= 8; i++) {
            if (Math.abs(i * 100) < 100) continue; 
            addBox(i * 100, 47, 0, 40, 2, 3, lineColor);
            addBox(0, 47, i * 100, 3, 2, 40, lineColor);
        }

        // Passages piétons (ajustés pour route 160)
        addCrosswalk(-140, 0, true);
        addCrosswalk(140, 0, true);
        addCrosswalk(0, -140, false);
        addCrosswalk(0, 140, false);
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
    //  FEUX TRICOLORES (3 sphères — orientés face aux voitures)
    // ═══════════════════════════════════════════════════════

    /**
     * Crée un feu tricolore orienté vers les voitures.
     * @param facingAngleDeg 0=face +X(Est), 90=face +Z(Sud), 180=face -X(Ouest), 270=face -Z(Nord)
     */
    public TrafficLight3D addTrafficLight(com.example.traffic.model.TrafficLight logicLight,
                                          double x, double z, double facingAngleDeg) {
        double rad = Math.toRadians(facingAngleDeg);
        double dx = Math.cos(rad) * 10; // offset des ampoules
        double dz = Math.sin(rad) * 10;

        // Poteau
        Cylinder pole = new Cylinder(3, 110);
        pole.setTranslateX(x);
        pole.setTranslateY(5);
        pole.setTranslateZ(z);
        pole.setMaterial(new PhongMaterial(Color.rgb(50, 50, 50)));

        // Boîtier (légèrement décalé vers la direction des voitures)
        Box box = new Box(18, 55, 14);
        box.setTranslateX(x + dx * 0.3);
        box.setTranslateY(-55);
        box.setTranslateZ(z + dz * 0.3);
        box.setMaterial(new PhongMaterial(Color.rgb(25, 25, 25)));
        box.setRotationAxis(javafx.scene.transform.Rotate.Y_AXIS);
        box.setRotate(facingAngleDeg);

        // Sphère ROUGE (en haut) — face aux voitures
        Sphere redLight = new Sphere(6);
        redLight.setTranslateX(x + dx);
        redLight.setTranslateY(-72);
        redLight.setTranslateZ(z + dz);
        redLight.setMaterial(new PhongMaterial(Color.DARKRED));

        // Sphère JAUNE (au milieu)
        Sphere yellowLight = new Sphere(6);
        yellowLight.setTranslateX(x + dx);
        yellowLight.setTranslateY(-55);
        yellowLight.setTranslateZ(z + dz);
        yellowLight.setMaterial(new PhongMaterial(Color.rgb(80, 70, 0)));

        // Sphère VERTE (en bas)
        Sphere greenLight = new Sphere(6);
        greenLight.setTranslateX(x + dx);
        greenLight.setTranslateY(-38);
        greenLight.setTranslateZ(z + dz);
        greenLight.setMaterial(new PhongMaterial(Color.DARKGREEN));

        this.getChildren().addAll(pole, box, redLight, yellowLight, greenLight);

        return new TrafficLight3D(logicLight, redLight, yellowLight, greenLight);
    }

    // ═══════════════════════════════════════════════════════
    //  VOITURES NORMALES
    // ═══════════════════════════════════════════════════════

    public Car3D addCar(com.example.traffic.model.Vehicle vehicle, Color bodyColor, Color topColor) {
        boolean isFuturistic = Math.random() > 0.7; // 30% de chance d'avoir une voiture futuriste
        
        if (isFuturistic) {
            return addFuturisticCar(vehicle);
        }

        PhongMaterial bodyMat = new PhongMaterial(bodyColor);
        bodyMat.setSpecularColor(Color.WHITE);
        bodyMat.setSpecularPower(30);

        PhongMaterial wheelMat = new PhongMaterial(Color.rgb(25, 25, 25));
        PhongMaterial mirrorMat = new PhongMaterial(bodyColor.darker());

        // Corps Central
        Box body = new Box(18, 12, 26);
        body.setMaterial(bodyMat);

        // Capot
        Box hood = new Box(18, 8, 24);
        hood.setMaterial(bodyMat);

        // Coffre
        Box trunk = new Box(18, 8, 24);
        trunk.setMaterial(bodyMat);

        // Habitacle
        Box cabin = new Box(24, 10, 22);
        cabin.setMaterial(new PhongMaterial(topColor));

        // Pare-brise
        Box windshield = new Box(2, 9, 20);
        windshield.setMaterial(new PhongMaterial(Color.rgb(180, 210, 255, 0.5)));

        // Phares
        Box headlightL = new Box(3, 5, 5);
        headlightL.setMaterial(new PhongMaterial(Color.WHITESMOKE));
        Box headlightR = new Box(3, 5, 5);
        headlightR.setMaterial(new PhongMaterial(Color.WHITESMOKE));

        // Feux arrière
        Box taillightL = new Box(2, 3, 5);
        taillightL.setMaterial(new PhongMaterial(Color.rgb(100, 0, 0)));
        Box taillightR = new Box(2, 3, 5);
        taillightR.setMaterial(new PhongMaterial(Color.rgb(100, 0, 0)));

        // Rétroviseurs
        Box mirrorL = new Box(2, 3, 4);
        mirrorL.setMaterial(mirrorMat);
        Box mirrorR = new Box(2, 3, 4);
        mirrorR.setMaterial(mirrorMat);

        // Roues avec jantes chromées premium
        java.util.List<javafx.scene.Node> wheels = new java.util.ArrayList<>();
        for (int i = 0; i < 4; i++) {
            wheels.add(createBeautifulWheel(7, 4, Color.rgb(25, 25, 25), Color.rgb(200, 200, 200)));
        }

        Car3D car = new Car3D(vehicle, body, hood, trunk, cabin, windshield, headlightL, headlightR, taillightL, taillightR, mirrorL, mirrorR, wheels);
        this.getChildren().addAll(car.getAllPartsAsList());

        return car;
    }

    private Car3D addFuturisticCar(com.example.traffic.model.Vehicle vehicle) {
        Color neonBlue = Color.rgb(0, 200, 255);
        PhongMaterial bodyMat = new PhongMaterial(Color.rgb(20, 30, 50)); // Sombre
        bodyMat.setSpecularColor(neonBlue);
        bodyMat.setSpecularPower(50);
        
        PhongMaterial accentMat = new PhongMaterial(Color.WHITE);
        PhongMaterial ledMat = new PhongMaterial(neonBlue);
        ledMat.setSelfIlluminationMap(null); // Pas de map, mais on peut simuler avec une couleur vive

        Box body = new Box(16, 10, 24);
        body.setMaterial(bodyMat);

        Box hood = new Box(20, 6, 22);
        hood.setMaterial(accentMat); // Blanc pour le capot style futuriste

        Box trunk = new Box(16, 6, 22);
        trunk.setMaterial(bodyMat);

        Box cabin = new Box(22, 12, 20);
        cabin.setMaterial(new PhongMaterial(Color.rgb(10, 10, 10, 0.9)));

        Box windshield = new Box(2, 11, 18);
        windshield.setMaterial(new PhongMaterial(Color.rgb(0, 255, 255, 0.4)));

        // LED Lights au lieu de phares classiques
        Box ledL = new Box(6, 1, 4);
        ledL.setMaterial(ledMat);
        Box ledR = new Box(6, 1, 4);
        ledR.setMaterial(ledMat);

        // LED Taillights (rouges/néon)
        PhongMaterial redLedMat = new PhongMaterial(Color.rgb(255, 50, 50));
        Box tailLedL = new Box(6, 1, 4);
        tailLedL.setMaterial(redLedMat);
        Box tailLedR = new Box(6, 1, 4);
        tailLedR.setMaterial(redLedMat);

        // Rétros fins
        Box mirrorL = new Box(1, 1, 6);
        mirrorL.setMaterial(ledMat);
        Box mirrorR = new Box(1, 1, 6);
        mirrorR.setMaterial(ledMat);

        // Roues néon futuristes
        java.util.List<javafx.scene.Node> wheels = new java.util.ArrayList<>();
        for (int i = 0; i < 4; i++) {
            wheels.add(createBeautifulWheel(7, 3, Color.rgb(10, 10, 10), neonBlue));
        }

        Car3D car = new Car3D(vehicle, body, hood, trunk, cabin, windshield, ledL, ledR, tailLedL, tailLedR, mirrorL, mirrorR, wheels);
        this.getChildren().addAll(body, hood, trunk, cabin, windshield, ledL, ledR, tailLedL, tailLedR, mirrorL, mirrorR);
        this.getChildren().addAll(wheels);
        return car;
    }

    // ═══════════════════════════════════════════════════════
    //  VOITURES D'URGENCE
    // ═══════════════════════════════════════════════════════

    public Car3D addEmergencyCar(com.example.traffic.model.Vehicle vehicle) {
        Color bodyColor = getEmergencyColor(vehicle.getEmergencyType());
        PhongMaterial bodyMat = new PhongMaterial(bodyColor);
        bodyMat.setSpecularColor(Color.WHITE);
        
        PhongMaterial wheelMat = new PhongMaterial(Color.rgb(15, 15, 15));

        Box body = new Box(22, 16, 28);
        body.setMaterial(bodyMat);

        Box hood = new Box(20, 12, 26);
        hood.setMaterial(bodyMat);

        Box trunk = new Box(20, 12, 26);
        trunk.setMaterial(bodyMat);

        Box cabin = new Box(26, 12, 24);
        cabin.setMaterial(new PhongMaterial(bodyColor.brighter()));

        Box windshield = new Box(2, 11, 22);
        windshield.setMaterial(new PhongMaterial(Color.rgb(150, 200, 255, 0.6)));

        Box headlightL = new Box(4, 5, 6);
        headlightL.setMaterial(new PhongMaterial(Color.WHITE));
        Box headlightR = new Box(4, 5, 6);
        headlightR.setMaterial(new PhongMaterial(Color.WHITE));

        Box sirenL = new Box(8, 8, 8);
        sirenL.setMaterial(new PhongMaterial(Color.RED));
        Box sirenR = new Box(8, 8, 8);
        sirenR.setMaterial(new PhongMaterial(Color.BLUE));

        Box mirrorL = new Box(3, 4, 5);
        mirrorL.setMaterial(new PhongMaterial(bodyColor.darker()));
        Box mirrorR = new Box(3, 4, 5);
        mirrorR.setMaterial(new PhongMaterial(bodyColor.darker()));

        // Roues robustes d'urgence
        java.util.List<javafx.scene.Node> wheels = new java.util.ArrayList<>();
        for (int i = 0; i < 4; i++) {
            wheels.add(createBeautifulWheel(8, 5, Color.rgb(15, 15, 15), Color.SILVER));
        }

        Car3D car = new Car3D(vehicle, body, hood, trunk, cabin, windshield, headlightL, headlightR, sirenL, sirenR, mirrorL, mirrorR, wheels);
        car.setEmergency(true);
        this.getChildren().addAll(body, hood, trunk, cabin, windshield, headlightL, headlightR, sirenL, sirenR, mirrorL, mirrorR);
        this.getChildren().addAll(wheels);

        return car;
    }

    /** Supprimer une voiture de la scène */
    public void removeCar(Car3D car) {
        for (javafx.scene.Node part : car.getAllPartsAsList()) {
            this.getChildren().remove(part);
        }
    }

    private Group createBeautifulWheel(double radius, double width, Color tireColor, Color rimColor) {
        Group wheelGroup = new Group();
        
        // Pneu (Tire)
        Cylinder tire = new Cylinder(radius, width);
        PhongMaterial tireMat = new PhongMaterial(tireColor);
        tire.setMaterial(tireMat);
        
        // Jante (Rim/Hubcap)
        Cylinder rim = new Cylinder(radius * 0.55, width + 0.4);
        PhongMaterial rimMat = new PhongMaterial(rimColor);
        rimMat.setSpecularColor(Color.WHITE);
        rimMat.setSpecularPower(40);
        rim.setMaterial(rimMat);
        
        wheelGroup.getChildren().addAll(tire, rim);
        return wheelGroup;
    }

    private Color getEmergencyColor(String type) {
        if (type == null) return AMBULANCE_COLOR;
        switch (type) {
            case "fire": return FIRE_COLOR;
            case "police": return POLICE_COLOR;
            case "rescue": return RESCUE_COLOR;
            default: return AMBULANCE_COLOR;
        }
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
        // Poser les bâtiments sur le sol (y=65)
        double y = 65 - h / 2;
        Box building = new Box(w, h, d);
        building.setTranslateX(x);
        building.setTranslateY(y);
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
