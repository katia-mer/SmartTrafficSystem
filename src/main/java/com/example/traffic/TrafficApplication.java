package com.example.traffic;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.shape.Cylinder;
import javafx.scene.shape.Sphere;
import javafx.scene.transform.Rotate;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;

public class TrafficApplication extends Application {

    private Group world;

    private final List<Car3D> cars = new ArrayList<>();
    private final List<TrafficLight3D> trafficLights = new ArrayList<>();

    private AnimationTimer timer;

    private boolean running = false;
    private long lastUpdate = 0;

    private Slider speedSlider;

    private boolean lightGreen = true;
    private double lightTimer = 0.0;

    private static final double ROAD_Y = 55;
    private static final double CAR_BODY_Y = 35;
    private static final double CAR_TOP_Y = 17;

    private static final double ROUNDABOUT_RADIUS = 120;
    private static final double SAFE_DISTANCE = 65;

    @Override
    public void start(Stage stage) {
        BorderPane root = new BorderPane();

        world = new Group();
        createWorld();

        SubScene subScene = new SubScene(world, 1050, 680, true, SceneAntialiasing.BALANCED);
        subScene.setFill(Color.rgb(18, 22, 32));

        PerspectiveCamera camera = new PerspectiveCamera(true);
        camera.setTranslateX(0);
        camera.setTranslateY(-720);
        camera.setTranslateZ(-950);
        camera.setRotationAxis(Rotate.X_AXIS);
        camera.setRotate(-43);
        camera.setNearClip(0.1);
        camera.setFarClip(5000);

        subScene.setCamera(camera);

        root.setCenter(subScene);
        root.setBottom(createControls());

        timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (!running) {
                    lastUpdate = now;
                    return;
                }

                if (lastUpdate == 0) {
                    lastUpdate = now;
                    return;
                }

                double deltaTime = (now - lastUpdate) / 1_000_000_000.0;
                lastUpdate = now;

                updateSimulation(deltaTime * speedSlider.getValue());
                updateTrafficLights();
                updateCars3D();
            }
        };

        timer.start();

        Scene scene = new Scene(root, 1050, 750);
        stage.setTitle("Smart Traffic System - Rond-point 3D");
        stage.setScene(scene);
        stage.show();

        updateTrafficLights();
        updateCars3D();
    }

    private void createWorld() {
        addLights();
        addGround();
        addRoads();
        addRoundabout();
        addRoadMarks();
        addTrafficLights();
        addCars();
        addBuildings();
        addTrees();
    }

    private void addLights() {
        AmbientLight ambientLight = new AmbientLight(Color.color(0.45, 0.45, 0.45));

        PointLight pointLight = new PointLight(Color.WHITE);
        pointLight.setTranslateX(-250);
        pointLight.setTranslateY(-650);
        pointLight.setTranslateZ(-500);

        world.getChildren().addAll(ambientLight, pointLight);
    }

    private void addGround() {
        Box ground = new Box(1150, 10, 1150);
        ground.setTranslateY(90);
        ground.setMaterial(new PhongMaterial(Color.rgb(35, 120, 55)));
        world.getChildren().add(ground);
    }

    private void addRoads() {
        // Route Ouest - Est
        addBox(0, ROAD_Y, 0, 950, 12, 110, Color.rgb(42, 42, 42));

        // Route Nord - Sud
        addBox(0, ROAD_Y, 0, 110, 12, 950, Color.rgb(42, 42, 42));

        // Trottoirs
        addBox(0, 47, -70, 950, 8, 18, Color.LIGHTGRAY);
        addBox(0, 47, 70, 950, 8, 18, Color.LIGHTGRAY);
        addBox(-70, 47, 0, 18, 8, 950, Color.LIGHTGRAY);
        addBox(70, 47, 0, 18, 8, 950, Color.LIGHTGRAY);
    }

    private void addRoundabout() {
        Cylinder roadCircle = new Cylinder(165, 14);
        roadCircle.setTranslateY(46);
        roadCircle.setMaterial(new PhongMaterial(Color.rgb(45, 45, 45)));

        Cylinder centerGrass = new Cylinder(82, 18);
        centerGrass.setTranslateY(35);
        centerGrass.setMaterial(new PhongMaterial(Color.rgb(40, 150, 65)));

        Cylinder decoration = new Cylinder(35, 24);
        decoration.setTranslateY(15);
        decoration.setMaterial(new PhongMaterial(Color.DARKOLIVEGREEN));

        Sphere statue = new Sphere(22);
        statue.setTranslateY(-15);
        statue.setMaterial(new PhongMaterial(Color.LIGHTBLUE));

        world.getChildren().addAll(roadCircle, centerGrass, decoration, statue);
    }

    private void addRoadMarks() {
        // Lignes centrales
        addBox(-310, 43, 0, 230, 3, 5, Color.WHITE);
        addBox(310, 43, 0, 230, 3, 5, Color.WHITE);
        addBox(0, 43, -310, 5, 3, 230, Color.WHITE);
        addBox(0, 43, 310, 5, 3, 230, Color.WHITE);

        // Lignes d'arrêt rouges avant le rond-point
        addBox(-210, 39, 25, 8, 5, 45, Color.RED);
        addBox(210, 39, -25, 8, 5, 45, Color.RED);
        addBox(-25, 39, -210, 45, 5, 8, Color.RED);
        addBox(25, 39, 210, 45, 5, 8, Color.RED);

        // Passages piétons
        addCrosswalkWestEast(-190, 0);
        addCrosswalkWestEast(190, 0);
        addCrosswalkNorthSouth(0, -190);
        addCrosswalkNorthSouth(0, 190);
    }

    private void addCrosswalkWestEast(double x, double z) {
        for (int i = 0; i < 6; i++) {
            addBox(x + i * 18, 40, z, 10, 4, 80, Color.WHITE);
        }
    }

    private void addCrosswalkNorthSouth(double x, double z) {
        for (int i = 0; i < 6; i++) {
            addBox(x, 40, z + i * 18, 80, 4, 10, Color.WHITE);
        }
    }

    private void addTrafficLights() {
        trafficLights.add(createTrafficLight(-230, -85)); // Ouest
        trafficLights.add(createTrafficLight(230, 85));   // Est
        trafficLights.add(createTrafficLight(85, -230));  // Nord
        trafficLights.add(createTrafficLight(-85, 230));  // Sud
    }

    private TrafficLight3D createTrafficLight(double x, double z) {
        Cylinder pole = new Cylinder(5, 120);
        pole.setTranslateX(x);
        pole.setTranslateY(0);
        pole.setTranslateZ(z);
        pole.setMaterial(new PhongMaterial(Color.BLACK));

        Box box = new Box(35, 70, 25);
        box.setTranslateX(x);
        box.setTranslateY(-75);
        box.setTranslateZ(z);
        box.setMaterial(new PhongMaterial(Color.rgb(20, 20, 20)));

        Sphere sphere = new Sphere(14);
        sphere.setTranslateX(x);
        sphere.setTranslateY(-75);
        sphere.setTranslateZ(z - 15);

        world.getChildren().addAll(pole, box, sphere);

        return new TrafficLight3D(sphere);
    }

    private void addCars() {
        cars.add(createCar(Route.WEST_TO_EAST, -440, Color.DODGERBLUE, Color.LIGHTBLUE));
        cars.add(createCar(Route.WEST_TO_EAST, -540, Color.ORANGE, Color.GOLD));
        cars.add(createCar(Route.WEST_TO_EAST, -640, Color.CRIMSON, Color.PINK));

        cars.add(createCar(Route.NORTH_TO_SOUTH, -440, Color.MEDIUMPURPLE, Color.PLUM));
        cars.add(createCar(Route.NORTH_TO_SOUTH, -560, Color.DEEPSKYBLUE, Color.LIGHTCYAN));

        cars.add(createCar(Route.SOUTH_TO_WEST, -450, Color.LIMEGREEN, Color.LIGHTGREEN));
        cars.add(createCar(Route.EAST_TO_NORTH, -500, Color.YELLOW, Color.LIGHTYELLOW));
    }

    private Car3D createCar(Route route, double distance, Color bodyColor, Color topColor) {
        Box body = new Box(45, 22, 28);
        body.setMaterial(new PhongMaterial(bodyColor));

        Box top = new Box(26, 16, 22);
        top.setMaterial(new PhongMaterial(topColor));

        Car3D car = new Car3D(route, distance, body, top);
        world.getChildren().addAll(body, top);

        return car;
    }

    private void updateSimulation(double deltaTime) {
        updateLightState(deltaTime);

        double speed = 90 * deltaTime;

        for (Car3D car : cars) {
            double nextDistance = car.distance + speed;

            if (mustStopAtRedLight(car, nextDistance)) {
                nextDistance = car.route.stopDistance();
            }

            Car3D frontCar = findFrontCar(car);
            if (frontCar != null) {
                double gap = frontCar.distance - car.distance;

                if (gap > 0 && gap < SAFE_DISTANCE) {
                    nextDistance = car.distance;
                }
            }

            car.distance = nextDistance;

            if (car.distance > car.route.totalLength()) {
                car.distance = car.route.startDistance();
            }
        }
    }

    private void updateLightState(double deltaTime) {
        lightTimer += deltaTime;

        if (lightTimer >= 5.0) {
            lightGreen = !lightGreen;
            lightTimer = 0.0;
        }
    }

    private boolean mustStopAtRedLight(Car3D car, double nextDistance) {
        if (lightGreen) {
            return false;
        }

        // Si la voiture est déjà dans le rond-point, elle ne s'arrête pas au milieu.
        if (car.distance >= car.route.entryDistance()) {
            return false;
        }

        // Si le feu est rouge, la voiture ne peut pas dépasser la ligne d'arrêt.
        return nextDistance >= car.route.stopDistance();
    }

    private Car3D findFrontCar(Car3D car) {
        Car3D front = null;
        double smallestGap = Double.MAX_VALUE;

        for (Car3D other : cars) {
            if (other == car) {
                continue;
            }

            if (other.route != car.route) {
                continue;
            }

            double gap = other.distance - car.distance;

            if (gap > 0 && gap < smallestGap) {
                smallestGap = gap;
                front = other;
            }
        }

        return front;
    }

    private void updateTrafficLights() {
        Color color = lightGreen ? Color.LIMEGREEN : Color.RED;

        for (TrafficLight3D trafficLight : trafficLights) {
            trafficLight.light.setMaterial(new PhongMaterial(color));
        }
    }

    private void updateCars3D() {
        for (Car3D car : cars) {
            CarPosition position = car.route.positionAt(car.distance);

            car.body.setTranslateX(position.x);
            car.body.setTranslateY(CAR_BODY_Y);
            car.body.setTranslateZ(position.z);
            car.body.setRotate(position.angle);

            car.top.setTranslateX(position.x);
            car.top.setTranslateY(CAR_TOP_Y);
            car.top.setTranslateZ(position.z);
            car.top.setRotate(position.angle);
        }
    }

    private HBox createControls() {
        Button startButton = new Button("Start");
        Button pauseButton = new Button("Pause");
        Button resetButton = new Button("Reset");

        speedSlider = new Slider(0.5, 5.0, 1.0);
        speedSlider.setShowTickLabels(true);
        speedSlider.setShowTickMarks(true);
        speedSlider.setMajorTickUnit(1.0);

        Label speedLabel = new Label("Vitesse");

        startButton.setOnAction(event -> running = true);

        pauseButton.setOnAction(event -> running = false);

        resetButton.setOnAction(event -> {
            running = false;
            lastUpdate = 0;
            lightGreen = true;
            lightTimer = 0.0;
            resetCars();
            updateTrafficLights();
            updateCars3D();
        });

        HBox controls = new HBox(12);
        controls.setPadding(new Insets(12));
        controls.setStyle("-fx-background-color: #dddddd;");
        controls.getChildren().addAll(startButton, pauseButton, resetButton, speedLabel, speedSlider);

        return controls;
    }

    private void resetCars() {
        cars.get(0).distance = -440;
        cars.get(1).distance = -540;
        cars.get(2).distance = -640;
        cars.get(3).distance = -440;
        cars.get(4).distance = -560;
        cars.get(5).distance = -450;
        cars.get(6).distance = -500;
    }

    private void addBuildings() {
        addBuilding(-390, -340, 110, 190, 110, Color.DARKSLATEBLUE);
        addBuilding(-190, -360, 120, 130, 100, Color.DARKRED);
        addBuilding(300, -360, 110, 220, 110, Color.DARKCYAN);
        addBuilding(450, -220, 100, 160, 100, Color.SADDLEBROWN);

        addBuilding(-410, 330, 120, 170, 110, Color.INDIGO);
        addBuilding(-190, 360, 90, 130, 90, Color.DIMGRAY);
        addBuilding(280, 330, 130, 190, 120, Color.DARKGOLDENROD);
        addBuilding(450, 220, 90, 140, 90, Color.DARKMAGENTA);
    }

    private void addBuilding(double x, double z, double width, double height, double depth, Color color) {
        Box building = new Box(width, height, depth);
        building.setTranslateX(x);
        building.setTranslateY(90 - height / 2);
        building.setTranslateZ(z);
        building.setMaterial(new PhongMaterial(color));
        world.getChildren().add(building);

        for (int i = 0; i < 3; i++) {
            Box window = new Box(12, 16, 3);
            window.setTranslateX(x - width / 4 + i * 25);
            window.setTranslateY(40);
            window.setTranslateZ(z - depth / 2 - 2);
            window.setMaterial(new PhongMaterial(Color.GOLD));
            world.getChildren().add(window);
        }
    }

    private void addTrees() {
        addTree(-480, -130);
        addTree(-480, 130);
        addTree(480, -130);
        addTree(480, 130);

        addTree(-130, -480);
        addTree(130, -480);
        addTree(-130, 480);
        addTree(130, 480);
    }

    private void addTree(double x, double z) {
        Cylinder trunk = new Cylinder(6, 45);
        trunk.setTranslateX(x);
        trunk.setTranslateY(55);
        trunk.setTranslateZ(z);
        trunk.setMaterial(new PhongMaterial(Color.SADDLEBROWN));

        Sphere leaves = new Sphere(25);
        leaves.setTranslateX(x);
        leaves.setTranslateY(20);
        leaves.setTranslateZ(z);
        leaves.setMaterial(new PhongMaterial(Color.FORESTGREEN));

        world.getChildren().addAll(trunk, leaves);
    }

    private void addBox(double x, double y, double z,
                        double width, double height, double depth,
                        Color color) {
        Box box = new Box(width, height, depth);
        box.setTranslateX(x);
        box.setTranslateY(y);
        box.setTranslateZ(z);
        box.setMaterial(new PhongMaterial(color));
        world.getChildren().add(box);
    }

    public static void main(String[] args) {
        launch(args);
    }

    private enum Route {
        WEST_TO_EAST {
            @Override
            CarPosition positionAt(double d) {
                if (d < -150) {
                    return new CarPosition(d, 25, 0);
                }

                if (d < 150) {
                    double theta = Math.toRadians(180 - ((d + 150) / 300) * 180);
                    double x = Math.cos(theta) * ROUNDABOUT_RADIUS;
                    double z = Math.sin(theta) * ROUNDABOUT_RADIUS;
                    double angle = -Math.toDegrees(theta) + 90;
                    return new CarPosition(x, z, angle);
                }

                return new CarPosition(d, -25, 0);
            }

            @Override
            double stopDistance() {
                return -210;
            }

            @Override
            double entryDistance() {
                return -150;
            }

            @Override
            double startDistance() {
                return -640;
            }

            @Override
            double totalLength() {
                return 520;
            }
        },

        NORTH_TO_SOUTH {
            @Override
            CarPosition positionAt(double d) {
                if (d < -150) {
                    return new CarPosition(-25, d, 90);
                }

                if (d < 150) {
                    double theta = Math.toRadians(270 - ((d + 150) / 300) * 180);
                    double x = Math.cos(theta) * ROUNDABOUT_RADIUS;
                    double z = Math.sin(theta) * ROUNDABOUT_RADIUS;
                    double angle = -Math.toDegrees(theta) + 90;
                    return new CarPosition(x, z, angle);
                }

                return new CarPosition(25, d, 90);
            }

            @Override
            double stopDistance() {
                return -210;
            }

            @Override
            double entryDistance() {
                return -150;
            }

            @Override
            double startDistance() {
                return -620;
            }

            @Override
            double totalLength() {
                return 520;
            }
        },

        SOUTH_TO_WEST {
            @Override
            CarPosition positionAt(double d) {
                if (d < -150) {
                    return new CarPosition(25, -d, -90);
                }

                if (d < 150) {
                    double theta = Math.toRadians(90 - ((d + 150) / 300) * 180);
                    double x = Math.cos(theta) * ROUNDABOUT_RADIUS;
                    double z = Math.sin(theta) * ROUNDABOUT_RADIUS;
                    double angle = -Math.toDegrees(theta) + 90;
                    return new CarPosition(x, z, angle);
                }

                return new CarPosition(-d, -25, 180);
            }

            @Override
            double stopDistance() {
                return -210;
            }

            @Override
            double entryDistance() {
                return -150;
            }

            @Override
            double startDistance() {
                return -620;
            }

            @Override
            double totalLength() {
                return 520;
            }
        },

        EAST_TO_NORTH {
            @Override
            CarPosition positionAt(double d) {
                if (d < -150) {
                    return new CarPosition(-d, -25, 180);
                }

                if (d < 150) {
                    double theta = Math.toRadians(0 - ((d + 150) / 300) * 180);
                    double x = Math.cos(theta) * ROUNDABOUT_RADIUS;
                    double z = Math.sin(theta) * ROUNDABOUT_RADIUS;
                    double angle = -Math.toDegrees(theta) + 90;
                    return new CarPosition(x, z, angle);
                }

                return new CarPosition(-25, -d, -90);
            }

            @Override
            double stopDistance() {
                return -210;
            }

            @Override
            double entryDistance() {
                return -150;
            }

            @Override
            double startDistance() {
                return -620;
            }

            @Override
            double totalLength() {
                return 520;
            }
        };

        abstract CarPosition positionAt(double distance);

        abstract double stopDistance();

        abstract double entryDistance();

        abstract double startDistance();

        abstract double totalLength();
    }

    private static class Car3D {
        private final Route route;
        private double distance;
        private final Box body;
        private final Box top;

        private Car3D(Route route, double distance, Box body, Box top) {
            this.route = route;
            this.distance = distance;
            this.body = body;
            this.top = top;
        }
    }

    private static class CarPosition {
        private final double x;
        private final double z;
        private final double angle;

        private CarPosition(double x, double z, double angle) {
            this.x = x;
            this.z = z;
            this.angle = angle;
        }
    }

    private static class TrafficLight3D {
        private final Sphere light;

        private TrafficLight3D(Sphere light) {
            this.light = light;
        }
    }
}