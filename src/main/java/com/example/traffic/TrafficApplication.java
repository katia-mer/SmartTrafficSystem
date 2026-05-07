package com.example.traffic;

import com.example.traffic.ai.QLearningAgent;
import com.example.traffic.simulation.SimulationEngine;
import com.example.traffic.ui.AnimationController3D;
import com.example.traffic.ui.CityEnvironment;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.PerspectiveCamera;
import javafx.scene.Scene;
import javafx.scene.SceneAntialiasing;
import javafx.scene.SubScene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.transform.Rotate;
import javafx.stage.Stage;

public class TrafficApplication extends Application {

    private AnimationTimer timer;
    private boolean running = false;
    private long lastUpdate = 0;
    private Slider speedSlider;

    private double mouseOldX, mouseOldY;
    private final Rotate rotateX = new Rotate(0, Rotate.X_AXIS);
    private final Rotate rotateY = new Rotate(0, Rotate.Y_AXIS);

    private AnimationController3D animationController;
    private Label aiStatusLabel;
    private Label aiScoreLabel;
    private Button aiButton;

    @Override
    public void start(Stage stage) {
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #1a1a2e;");

        CityEnvironment world = new CityEnvironment();
        world.getTransforms().addAll(rotateX, rotateY);

        animationController = new AnimationController3D(world);

        SubScene subScene = new SubScene(world, 900, 560, true, SceneAntialiasing.BALANCED);
        subScene.setFill(Color.rgb(12, 16, 28));

        PerspectiveCamera camera = new PerspectiveCamera(true);
        camera.setTranslateX(0);
        camera.setTranslateY(-720);
        camera.setTranslateZ(-950);
        camera.setRotationAxis(Rotate.X_AXIS);
        camera.setRotate(-43);
        camera.setNearClip(0.1);
        camera.setFarClip(5000);

        subScene.setCamera(camera);

        subScene.setOnMousePressed(event -> {
            mouseOldX = event.getSceneX();
            mouseOldY = event.getSceneY();
        });

        subScene.setOnMouseDragged(event -> {
            double mousePosX = event.getSceneX();
            double mousePosY = event.getSceneY();

            rotateY.setAngle(rotateY.getAngle() - (mousePosX - mouseOldX) * 0.3);
            rotateX.setAngle(rotateX.getAngle() + (mousePosY - mouseOldY) * 0.3);

            mouseOldX = mousePosX;
            mouseOldY = mousePosY;
        });

        subScene.setOnScroll(event -> {
            double zoomFactor = event.getDeltaY() > 0 ? 0.95 : 1.05;
            camera.setTranslateY(camera.getTranslateY() * zoomFactor);
            camera.setTranslateZ(camera.getTranslateZ() * zoomFactor);
        });

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

                animationController.update(deltaTime * speedSlider.getValue());

                // Mise à jour du label IA
                updateAILabels();
            }
        };

        timer.start();

        Scene scene = new Scene(root, 900, 640);
        stage.setTitle("🚦 Smart Traffic System — IA & Simulation 3D");
        stage.setScene(scene);
        stage.show();

        animationController.updateTrafficLights();
        animationController.updateCars3D();
    }

    private VBox createControls() {
        // ── Ligne 1 : Boutons principaux ──
        Button startButton = createStyledButton("▶ Start", "#27ae60");
        Button pauseButton = createStyledButton("⏸ Pause", "#e67e22");
        Button resetButton = createStyledButton("⟲ Reset", "#c0392b");

        speedSlider = new Slider(0.5, 5.0, 1.0);
        speedSlider.setShowTickLabels(true);
        speedSlider.setShowTickMarks(true);
        speedSlider.setMajorTickUnit(1.0);
        speedSlider.setPrefWidth(160);
        speedSlider.setStyle("-fx-control-inner-background: #2c3e50;");

        Label speedLabel = new Label("Vitesse");
        speedLabel.setTextFill(Color.WHITE);
        speedLabel.setFont(Font.font("System", FontWeight.BOLD, 12));

        startButton.setOnAction(event -> running = true);
        pauseButton.setOnAction(event -> running = false);
        resetButton.setOnAction(event -> {
            running = false;
            lastUpdate = 0;
            animationController.reset();
            updateAILabels();
        });

        HBox mainControls = new HBox(10, startButton, pauseButton, resetButton, speedLabel, speedSlider);
        mainControls.setAlignment(Pos.CENTER_LEFT);

        // ── Ligne 2 : Contrôles IA ──
        aiButton = createStyledButton("🧠 Mode IA : OFF", "#8e44ad");
        aiButton.setPrefWidth(180);
        aiButton.setOnAction(event -> toggleAI());

        aiStatusLabel = new Label("Mode : Timer classique");
        aiStatusLabel.setTextFill(Color.LIGHTGRAY);
        aiStatusLabel.setFont(Font.font("System", 11));

        aiScoreLabel = new Label("");
        aiScoreLabel.setTextFill(Color.rgb(100, 220, 255));
        aiScoreLabel.setFont(Font.font("System", FontWeight.BOLD, 11));

        HBox aiControls = new HBox(15, aiButton, aiStatusLabel, aiScoreLabel);
        aiControls.setAlignment(Pos.CENTER_LEFT);

        VBox controls = new VBox(6, mainControls, aiControls);
        controls.setPadding(new Insets(10, 15, 10, 15));
        controls.setStyle("-fx-background-color: linear-gradient(to bottom, #2c3e50, #1a252f); " +
                "-fx-border-color: #34495e; -fx-border-width: 1 0 0 0;");

        return controls;
    }

    private Button createStyledButton(String text, String color) {
        Button btn = new Button(text);
        btn.setStyle("-fx-background-color: " + color + "; " +
                "-fx-text-fill: white; " +
                "-fx-font-weight: bold; " +
                "-fx-font-size: 12px; " +
                "-fx-padding: 6 16; " +
                "-fx-background-radius: 6; " +
                "-fx-cursor: hand;");
        btn.setOnMouseEntered(e -> btn.setStyle(btn.getStyle() + "-fx-opacity: 0.85;"));
        btn.setOnMouseExited(e -> btn.setStyle(btn.getStyle().replace("-fx-opacity: 0.85;", "")));
        return btn;
    }

    private void toggleAI() {
        SimulationEngine engine = animationController.getSimulationEngine();
        boolean newMode = !engine.isAiMode();
        engine.setAiMode(newMode);

        if (newMode) {
            aiButton.setText("🧠 Mode IA : ON");
            aiButton.setStyle(aiButton.getStyle().replace("#8e44ad", "#2ecc71"));
            aiStatusLabel.setText("Mode : Q-Learning actif");
            aiStatusLabel.setTextFill(Color.LIMEGREEN);
        } else {
            aiButton.setText("🧠 Mode IA : OFF");
            aiButton.setStyle(aiButton.getStyle().replace("#2ecc71", "#8e44ad"));
            aiStatusLabel.setText("Mode : Timer classique");
            aiStatusLabel.setTextFill(Color.LIGHTGRAY);
            aiScoreLabel.setText("");
        }
    }

    private void updateAILabels() {
        SimulationEngine engine = animationController.getSimulationEngine();
        if (engine.isAiMode() && engine.getAgent() != null) {
            QLearningAgent agent = engine.getAgent();
            aiScoreLabel.setText(String.format(
                    "Score: %.1f | Étapes: %d | Exploration: %.0f%% | États appris: %d",
                    agent.getTotalReward(),
                    agent.getTotalSteps(),
                    agent.getEpsilon() * 100,
                    agent.getQTableSize()));
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
