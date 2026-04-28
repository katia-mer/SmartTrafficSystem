package com.example.traffic;

import com.example.traffic.modele.Intersection;
import com.example.traffic.modele.Vehicule;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.control.Label;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import java.util.HashMap;
import java.util.Map;

public class Main extends Application {

    private Intersection intersection = new Intersection();
    private Map<Vehicule, ImageView> visuels = new HashMap<>();

    @Override
    public void start(Stage stage) {

        Pane root = new Pane();

        // 🛣️ routes
        Rectangle roadH = new Rectangle(0, 200, 800, 100);
        roadH.setFill(Color.GRAY);

        Rectangle roadV = new Rectangle(350, 0, 100, 400);
        roadV.setFill(Color.DARKGRAY);

        // 🚦 feu
        Circle feuUI = new Circle(20);
        feuUI.setCenterX(300);
        feuUI.setCenterY(180);

        root.getChildren().addAll(roadH, roadV, feuUI);

        // 📊 DASHBOARD
        VBox dashboard = new VBox();
        dashboard.setLayoutX(10);
        dashboard.setLayoutY(10);
        dashboard.setSpacing(5);

        dashboard.setStyle(
                "-fx-background-color: rgba(255,255,255,0.8);" +
                        "-fx-padding: 10;" +
                        "-fx-border-color: black;"
        );

        Label title = new Label("SMART TRAFFIC ");
        title.setFont(new Font(16));

        Label veh = new Label();
        Label feu = new Label();
        Label pass = new Label();
        Label time = new Label();

        dashboard.getChildren().addAll(title, veh, feu, pass, time);
        root.getChildren().add(dashboard);

        // 🖼️ voiture image
        var url = getClass().getResource("/com/example/traffic/images/car.png");
        Image carImage = new Image(url.toExternalForm());

        // 🚗 voitures horizontales
        for (int i = 0; i < 3; i++) {
            Vehicule v = new Vehicule(-i * 120, 240, 2, true);
            intersection.ajouterVehicule(v);

            ImageView iv = new ImageView(carImage);
            iv.setFitWidth(40);
            iv.setFitHeight(25);

            visuels.put(v, iv);
            root.getChildren().add(iv);
        }

        // 🚗 voitures verticales
        for (int i = 0; i < 3; i++) {
            Vehicule v = new Vehicule(380, -i * 120, 2, false);
            intersection.ajouterVehicule(v);

            ImageView iv = new ImageView(carImage);
            iv.setFitWidth(40);
            iv.setFitHeight(25);

            visuels.put(v, iv);
            root.getChildren().add(iv);
        }

        Scene scene = new Scene(root, 800, 400);

        stage.setTitle("Smart Traffic System");
        stage.setScene(scene);
        stage.show();

        // 🔄 LOOP
        AnimationTimer timer = new AnimationTimer() {
            @Override
            public void handle(long now) {

                intersection.mettreAJour();

                // 🚦 feu
                feuUI.setFill(intersection.getFeu().estVert() ? Color.GREEN : Color.RED);

                // 🚗 voitures
                for (Vehicule v : intersection.getVehicules()) {
                    ImageView iv = visuels.get(v);
                    iv.setLayoutX(v.getPositionX());
                    iv.setLayoutY(v.getPositionY());
                }

                // 📊 dashboard
                veh.setText("🚗 Véhicules: " + intersection.getNombreVehicules());
                feu.setText("🚦 Feu: " + (intersection.getFeu().estVert() ? "VERT" : "ROUGE"));
                pass.setText("🚗 Passées: " + intersection.getVoituresPassees());
                time.setText("⏱ Temps: " + intersection.getTempsEcoule() + "s");
            }
        };

        timer.start();
    }

    public static void main(String[] args) {
        launch();
    }
}