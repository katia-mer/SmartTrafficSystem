package com.example.traffic;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;

public class Main extends Application {

    double carX = 0;

    @Override
    public void start(Stage stage) {

        Pane root = new Pane();

        // 🛣️ Route
        Rectangle road = new Rectangle(0, 150, 800, 100);
        road.setFill(Color.GRAY);

        // 🚗 Voiture
        Rectangle car = new Rectangle(50, 30);
        car.setFill(Color.RED);
        car.setX(carX);
        car.setY(185);

        root.getChildren().addAll(road, car);

        Scene scene = new Scene(root, 800, 400);

        stage.setTitle("Smart Traffic System - Step 2");
        stage.setScene(scene);
        stage.show();

        // 🧠 Animation
        AnimationTimer timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                carX += 2;

                if (carX > 800) {
                    carX = -50;
                }

                car.setX(carX);
            }
        };

        timer.start();
    }

    public static void main(String[] args) {
        launch();
    }
}