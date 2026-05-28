package com.example.traffic;

import com.example.traffic.ai.QLearningAgent;
import com.example.traffic.model.TrafficLight;
import com.example.traffic.simulation.SimulationBenchmark;
import com.example.traffic.simulation.SimulationEngine;
import com.example.traffic.ui.AnimationController3D;
import com.example.traffic.ui.CityEnvironment;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.*;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Slider;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.transform.Rotate;
import javafx.stage.Stage;

import java.util.List;

public class TrafficApplication extends Application {

    private AnimationTimer timer;
    private boolean running = false;
    private long lastUpdate = 0;
    private Slider speedSlider;
    private double mouseOldX, mouseOldY;
    private final Rotate rotateX = new Rotate(0, Rotate.X_AXIS);
    private final Rotate rotateY = new Rotate(0, Rotate.Y_AXIS);
    private AnimationController3D animationController;
    private CityEnvironment world;
    private SubScene subScene;

    // UI Labels
    private Label fpsLabel, carsLabel, waitNSLabel, waitEWLabel;
    private Circle nsRed, nsYellow, nsGreen, ewRed, ewYellow, ewGreen;
    private Label aiStatusLabel, aiScoreLabel, aiDecisionLabel, aiConfidenceLabel, aiReasonLabel;
    private Button aiButton;
    private VBox aiLogBox;
    private Label emergencyLabel;
    private HBox emergencyAlert;
    private Button btnLow, btnMed, btnHigh;
    private Button btnRain, btnNight, btnRush;
    private Button btnGraphs, btnBenchmark;

    // FPS
    private int frameCount = 0;
    private long lastFpsTime = 0;

    @Override
    public void start(Stage stage) {
        StackPane root = new StackPane();
        root.setStyle("-fx-background-color: #0f172a;");

        world = new CityEnvironment();
        world.getTransforms().addAll(rotateX, rotateY);
        animationController = new AnimationController3D(world);

        subScene = new SubScene(world, 1100, 700, true, SceneAntialiasing.BALANCED);
        subScene.setFill(Color.rgb(200, 210, 220));

        PerspectiveCamera camera = new PerspectiveCamera(true);
        camera.setTranslateX(0);
        camera.setTranslateY(-720);
        camera.setTranslateZ(-950);
        camera.setRotationAxis(Rotate.X_AXIS);
        camera.setRotate(-43);
        camera.setNearClip(0.1);
        camera.setFarClip(5000);
        subScene.setCamera(camera);

        subScene.setOnMousePressed(e -> {
            mouseOldX = e.getSceneX();
            mouseOldY = e.getSceneY();
        });
        subScene.setOnMouseDragged(e -> {
            rotateY.setAngle(rotateY.getAngle() - (e.getSceneX() - mouseOldX) * 0.3);
            rotateX.setAngle(rotateX.getAngle() + (e.getSceneY() - mouseOldY) * 0.3);
            mouseOldX = e.getSceneX();
            mouseOldY = e.getSceneY();
        });
        subScene.setOnScroll(e -> {
            double f = e.getDeltaY() > 0 ? 0.95 : 1.05;
            camera.setTranslateY(camera.getTranslateY() * f);
            camera.setTranslateZ(camera.getTranslateZ() * f);
        });

        // Overlay
        BorderPane overlay = new BorderPane();
        overlay.setPickOnBounds(false);
        overlay.setPadding(new Insets(12));

        overlay.setTop(createTopBar());
        overlay.setRight(createRightPanel());
        overlay.setBottom(createBottomBar());

        emergencyAlert = createEmergencyAlert();
        emergencyAlert.setVisible(false);
        StackPane.setAlignment(emergencyAlert, Pos.BOTTOM_LEFT);
        StackPane.setMargin(emergencyAlert, new Insets(0, 0, 80, 20));

        root.getChildren().addAll(subScene, overlay, emergencyAlert);

        // Bind SubScene size
        subScene.widthProperty().bind(root.widthProperty());
        subScene.heightProperty().bind(root.heightProperty());

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
                double dt = (now - lastUpdate) / 1_000_000_000.0;
                lastUpdate = now;
                animationController.update(dt * speedSlider.getValue());
                updateUI(now);
            }
        };
        timer.start();

        Scene scene = new Scene(root, 1100, 700);
        stage.setTitle("\uD83D\uDEA6 Simulateur Trafic Urbain IA — Règles Françaises");
        stage.setScene(scene);
        stage.show();
        animationController.updateTrafficLights();
        animationController.updateCars3D();
    }

    // ═══ TOP BAR (Title + Traffic Volume) ═══
    private HBox createTopBar() {
        // --- TITLE PANEL (Top Left) ---
        VBox titleBox = new VBox(2);
        Label title = new Label("Simulateur Trafic Urbain IA");
        title.setFont(Font.font("System", FontWeight.BOLD, 22));
        title.setTextFill(Color.rgb(56, 189, 248));
        Label subtitle = new Label("Règles Françaises • Modèle IDM");
        subtitle.setFont(Font.font("System", 12));
        subtitle.setTextFill(Color.rgb(100, 116, 139));
        titleBox.getChildren().addAll(title, subtitle);
        HBox titlePanel = wrapPanel(titleBox);
        titlePanel.setPadding(new Insets(15, 25, 15, 25));

        // --- TRAFFIC PANEL (Top Right) ---
        VBox trafficGroup = new VBox(8);
        Label trafficLabel = new Label("VOLUME TRAFIC");
        trafficLabel.setFont(Font.font("System", FontWeight.BOLD, 10));
        trafficLabel.setTextFill(Color.rgb(148, 163, 184));

        btnLow = makeToggleBtn("Faible", false);
        btnMed = makeToggleBtn("Moyen", true);
        btnHigh = makeToggleBtn("Élevé", false);
        btnLow.setOnAction(e -> setTrafficLevel(SimulationEngine.TrafficLevel.LOW));
        btnMed.setOnAction(e -> setTrafficLevel(SimulationEngine.TrafficLevel.MED));
        btnHigh.setOnAction(e -> setTrafficLevel(SimulationEngine.TrafficLevel.HIGH));

        HBox btnRow = new HBox(0, btnLow, btnMed, btnHigh);
        btnRow.setStyle("-fx-background-color: #0f172a; -fx-background-radius: 8; -fx-padding: 2;");

        aiButton = new Button("Optimisation IA : OFF");
        aiButton.setFocusTraversable(false);
        aiButton.setStyle("-fx-background-color: #334155; -fx-text-fill: #94a3b8; -fx-font-weight: bold; " +
                "-fx-font-size: 13px; -fx-padding: 10 25; -fx-background-radius: 10; -fx-cursor: hand;");
        aiButton.setOnAction(e -> toggleAI());

        // Initialisation des labels IA pour éviter les crashs
        aiStatusLabel = new Label();
        aiScoreLabel = new Label();
        aiDecisionLabel = new Label();
        aiConfidenceLabel = new Label();
        aiReasonLabel = new Label("En attente d'activation...");
        aiLogBox = new VBox(2);

        HBox trafficControls = new HBox(25, btnRow, aiButton);
        trafficControls.setAlignment(Pos.CENTER_LEFT);
        trafficGroup.getChildren().addAll(trafficLabel, trafficControls);
        HBox trafficPanel = wrapPanel(trafficGroup);
        trafficPanel.setPadding(new Insets(15, 25, 15, 25));

        // --- CONTROLS PANEL (Top Right) ---
        VBox controlsGroup = new VBox(8);
        Label controlsLabel = new Label("COMMANDES SIMULATION");
        controlsLabel.setFont(Font.font("System", FontWeight.BOLD, 10));
        controlsLabel.setTextFill(Color.rgb(148, 163, 184));

        Button startBtn = makeBtn("▶ Start", "#22c55e");
        Button pauseBtn = makeBtn("⏸ Pause", "#f59e0b");
        startBtn.setOnAction(e -> running = true);
        pauseBtn.setOnAction(e -> running = false);

        speedSlider = new Slider(0.5, 5.0, 0.7);
        speedSlider.setPrefWidth(120);
        speedSlider.setShowTickLabels(false);
        speedSlider.setFocusTraversable(false);
        speedSlider.setStyle("-fx-control-inner-background: #1e293b;");

        HBox controlBtns = new HBox(12, startBtn, pauseBtn, speedSlider);
        controlBtns.setAlignment(Pos.CENTER_LEFT);
        controlsGroup.getChildren().addAll(controlsLabel, controlBtns);
        HBox controlsPanel = wrapPanel(controlsGroup);
        controlsPanel.setPadding(new Insets(15, 25, 15, 25));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox topBar = new HBox(12, trafficPanel, spacer, controlsPanel);
        topBar.setAlignment(Pos.TOP_LEFT);
        return topBar;
    }

    // ═══ RIGHT PANEL (Telemetry Bottom Right) ═══
    private VBox createRightPanel() {
        fpsLabel = statValue("60");
        carsLabel = statValue("0");
        waitNSLabel = statValue("0.0s");
        waitEWLabel = statValue("0.0s");

        VBox carsBox = statBox(carsLabel, "VÉHICULES ACTIFS");
        carsBox.setPrefWidth(230);
        carsBox.setAlignment(Pos.CENTER);

        HBox waitBox = new HBox(10, 
                statBox(waitNSLabel, "ATTENTE MOY. (N/S)"),
                statBox(waitEWLabel, "ATTENTE MOY. (E/O)")
        );

        VBox stats = new VBox(10, carsBox, waitBox);

        HBox lightsRow = new HBox(12);
        lightsRow.setAlignment(Pos.CENTER_LEFT);
        Label nsLbl = new Label("N/S:");
        nsLbl.setTextFill(Color.rgb(148, 163, 184));
        nsLbl.setFont(Font.font(11));
        nsRed = light(Color.rgb(60, 20, 20));
        nsYellow = light(Color.rgb(60, 50, 0));
        nsGreen = light(Color.rgb(10, 60, 10));
        Label ewLbl = new Label("E/O:");
        ewLbl.setTextFill(Color.rgb(148, 163, 184));
        ewLbl.setFont(Font.font(11));
        ewRed = light(Color.rgb(60, 20, 20));
        ewYellow = light(Color.rgb(60, 50, 0));
        ewGreen = light(Color.rgb(10, 60, 10));
        lightsRow.getChildren().addAll(nsLbl, nsRed, nsYellow, nsGreen, ewLbl, ewRed, ewYellow, ewGreen);

        VBox telemetry = new VBox(15, stats, lightsRow);

        // Ajout d'un petit log IA
        Label logTitle = new Label("LOG IA");
        logTitle.setFont(Font.font("System", FontWeight.BOLD, 9));
        logTitle.setTextFill(Color.rgb(100, 116, 139));

        ScrollPane logScroll = new ScrollPane(aiLogBox);
        logScroll.setPrefHeight(100);
        logScroll.setFitToWidth(true);
        logScroll.setStyle(
                "-fx-background: transparent; -fx-background-color: transparent; -fx-border-color: rgba(255,255,255,0.05);");

        telemetry.getChildren().addAll(logTitle, logScroll);

        HBox telPanel = wrapPanel(telemetry);
        telPanel.setPadding(new Insets(15));

        VBox rightPanel = new VBox(telPanel);
        rightPanel.setAlignment(Pos.BOTTOM_RIGHT);
        BorderPane.setMargin(rightPanel, new Insets(0, 0, 0, 0));

        // On wrap dans un Pane pour forcer l'alignement en bas à droite
        VBox container = new VBox(rightPanel);
        container.setAlignment(Pos.BOTTOM_RIGHT);
        return container;
    }

    // ═══ BOTTOM BAR (Scenario Buttons) ═══
    private HBox createBottomBar() {
        // Scénarios d'urgence
        Button emergencyBtn = makeScenarioBtn("🚨 Urgence", "#ef4444", "#dc2626");
        emergencyBtn.setOnAction(e -> triggerEmergency());

        // Scénarios environnement
        btnRush = makeScenarioBtn("🚗 Heure de Pointe", "#854d0e", "#713f12");
        btnRush.setOnAction(e -> toggleRushHour());

        Button resetBtn = makeScenarioBtn("↻ Reset", "#475569", "#334155");
        resetBtn.setOnAction(e -> resetSimulation());

        // Météo / Temps
        btnRain = makeScenarioBtn("🌧️ Pluie", "#334155", "#1e293b");
        btnRain.setOnAction(e -> toggleRain());

        btnNight = makeScenarioBtn("🌙 Nuit", "#4c1d95", "#2e1065");
        btnNight.setOnAction(e -> toggleNight());

        btnGraphs = makeScenarioBtn("📊 Stats Session", "#0f766e", "#115e59");
        btnGraphs.setOnAction(e -> runComparisonGraphs());

        btnBenchmark = makeScenarioBtn("🧪 Benchmark IA", "#7c3aed", "#5b21b6");
        btnBenchmark.setOnAction(e -> runFullBenchmark());

        HBox scenariosRow = new HBox(15, emergencyBtn, btnRush, btnRain, btnNight, resetBtn, btnGraphs, btnBenchmark);
        scenariosRow.setAlignment(Pos.CENTER);

        VBox scenariosContainer = new VBox(12, scenariosRow);
        scenariosContainer.setAlignment(Pos.CENTER);
        scenariosContainer.setPadding(new Insets(20));

        HBox bar = new HBox(scenariosContainer);
        bar.setAlignment(Pos.CENTER);
        return bar;
    }

    // ═══ EMERGENCY ALERT ═══
    private HBox createEmergencyAlert() {
        emergencyLabel = new Label("\uD83D\uDEA8 Véhicule d'urgence en approche!");
        emergencyLabel.setTextFill(Color.WHITE);
        emergencyLabel.setFont(Font.font("System", FontWeight.BOLD, 11));
        HBox alert = new HBox(8, emergencyLabel);
        alert.setStyle("-fx-background-color: rgba(239,68,68,0.95); -fx-padding: 10 16; -fx-background-radius: 8;");
        alert.setMaxWidth(350);
        alert.setMaxHeight(40);
        return alert;
    }

    // ═══ ACTIONS ═══
    private void toggleAI() {
        SimulationEngine engine = animationController.getSimulationEngine();
        boolean newMode = !engine.isAiMode();
        engine.setAiMode(newMode);
        if (newMode) {
            aiButton.setStyle(
                    "-fx-background-color: #0ea5e9; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 13px; -fx-padding: 10 25; -fx-background-radius: 10; -fx-cursor: hand;");
            aiButton.setText("Optimisation IA : ON");
        } else {
            aiButton.setStyle(
                    "-fx-background-color: #334155; -fx-text-fill: #94a3b8; -fx-font-weight: bold; -fx-font-size: 13px; -fx-padding: 10 25; -fx-background-radius: 10; -fx-cursor: hand;");
            aiButton.setText("Optimisation IA : OFF");
        }
    }

    private void setTrafficLevel(SimulationEngine.TrafficLevel level) {
        animationController.getSimulationEngine().setTrafficLevel(level);
        btnLow.setStyle(toggleStyle(level == SimulationEngine.TrafficLevel.LOW));
        btnMed.setStyle(toggleStyle(level == SimulationEngine.TrafficLevel.MED));
        btnHigh.setStyle(toggleStyle(level == SimulationEngine.TrafficLevel.HIGH));
    }

    private void triggerEmergency() {
        SimulationEngine engine = animationController.getSimulationEngine();
        if (engine.getActiveEmergency() != null)
            return;
        engine.spawnEmergencyVehicle();
        emergencyAlert.setVisible(true);
        emergencyLabel.setText("\uD83D\uDEA8 Véhicule d'urgence en approche!");
    }

    private void toggleRain() {
        SimulationEngine engine = animationController.getSimulationEngine();
        boolean rain = !engine.isRainMode();
        engine.setRainMode(rain);

        // Update 3D visuals
        world.setRainMode(rain);
        subScene.setFill(rain ? Color.rgb(70, 80, 95) : Color.rgb(135, 206, 235));

        btnRain.setStyle(rain
                ? "-fx-background-color: rgba(59,130,246,0.5); -fx-text-fill: #93c5fd; -fx-font-weight: bold; -fx-font-size: 11px; -fx-padding: 8 14; -fx-background-radius: 8; -fx-cursor: hand;"
                : "-fx-background-color: rgba(59,130,246,0.2); -fx-text-fill: #60a5fa; -fx-font-weight: bold; -fx-font-size: 11px; -fx-padding: 8 14; -fx-background-radius: 8; -fx-cursor: hand;");
    }

    private void toggleNight() {
        SimulationEngine engine = animationController.getSimulationEngine();
        boolean night = !engine.isNightMode();
        engine.setNightMode(night);

        // Update 3D environment lighting
        world.setNightMode(night);
        animationController.setNightMode(night);

        // Update SubScene sky color
        subScene.setFill(night ? Color.rgb(10, 15, 30) : Color.rgb(135, 206, 235));

        btnNight.setStyle(night
                ? "-fx-background-color: rgba(139,92,246,0.5); -fx-text-fill: #c4b5fd; -fx-font-weight: bold; -fx-font-size: 11px; -fx-padding: 8 14; -fx-background-radius: 8; -fx-cursor: hand;"
                : "-fx-background-color: rgba(139,92,246,0.2); -fx-text-fill: #a78bfa; -fx-font-weight: bold; -fx-font-size: 11px; -fx-padding: 8 14; -fx-background-radius: 8; -fx-cursor: hand;");
    }

    private void toggleRushHour() {
        SimulationEngine engine = animationController.getSimulationEngine();
        engine.setRushHour(!engine.isRushHour());
        btnRush.setStyle(engine.isRushHour()
                ? "-fx-background-color: rgba(245,158,11,0.5); -fx-text-fill: #fcd34d; -fx-font-weight: bold; -fx-font-size: 11px; -fx-padding: 8 14; -fx-background-radius: 8; -fx-cursor: hand;"
                : "-fx-background-color: rgba(245,158,11,0.2); -fx-text-fill: #fbbf24; -fx-font-weight: bold; -fx-font-size: 11px; -fx-padding: 8 14; -fx-background-radius: 8; -fx-cursor: hand;");
    }

    private void resetSimulation() {
        running = false;
        lastUpdate = 0;
        animationController.reset();
        emergencyAlert.setVisible(false);
        aiLogBox.getChildren().clear();
        aiReasonLabel.setText("Simulation réinitialisée...");
        aiDecisionLabel.setText("En attente...");
    }

    private void runComparisonGraphs() {
        SimulationEngine engine = animationController.getSimulationEngine();
        List<SimulationBenchmark.DataPoint> historyCopy = new java.util.ArrayList<>(engine.getHistory());

        if (historyCopy.isEmpty()) {
            engine.logAI("⚠️ Pas encore de données. Lancez 'Start' pendant quelques secondes.");
            return;
        }

        btnGraphs.setDisable(true);
        btnGraphs.setText("Calcul...");

        Thread worker = new Thread(() -> {
            try {
                // Récupérer les paramètres de la session actuelle
                SimulationEngine.TrafficLevel level = engine.getTrafficLevel();
                boolean rush = engine.isRushHour();
                double duration = historyCopy.get(historyCopy.size() - 1).timeSeconds;

                // Générer un baseline classique pour comparer
                List<SimulationBenchmark.DataPoint> classicBaseline = SimulationBenchmark.runScenario(
                        false, 42L, level, rush, duration);

                Platform.runLater(() -> {
                    SimulationBenchmark.ComparisonResult result;
                    if (engine.isAiMode()) {
                        result = new SimulationBenchmark.ComparisonResult(classicBaseline, historyCopy);
                    } else {
                        result = new SimulationBenchmark.ComparisonResult(historyCopy, new java.util.ArrayList<>());
                    }
                    showComparisonCharts(result);
                });
            } finally {
                Platform.runLater(() -> {
                    btnGraphs.setDisable(false);
                    btnGraphs.setText("📊 Stats Session");
                });
            }
        }, "traffic-session-stats");
        worker.setDaemon(true);
        worker.start();
    }

    private void runFullBenchmark() {
        animationController.getSimulationEngine().logAI("🧪 Lancement du Benchmark IA vs Classique...");
        btnBenchmark.setDisable(true);
        btnBenchmark.setText("Calcul...");

        Thread worker = new Thread(() -> {
            try {
                SimulationBenchmark.ComparisonResult result = SimulationBenchmark.runDefault();
                Platform.runLater(() -> {
                    showComparisonCharts(result);
                    animationController.getSimulationEngine().logAI("✅ Benchmark terminé.");
                });
            } finally {
                Platform.runLater(() -> {
                    btnBenchmark.setDisable(false);
                    btnBenchmark.setText("🧪 Benchmark IA");
                });
            }
        }, "traffic-full-benchmark");
        worker.setDaemon(true);
        worker.start();
    }

    private void showComparisonCharts(SimulationBenchmark.ComparisonResult result) {
        LineChart<Number, Number> waitChart = createLineChart(
                "Evolution du temps d'attente moyen",
                "Temps de simulation (s)",
                "Attente moyenne (s)"
        );
        addSeries(waitChart, "Systeme classique", result.classic, true);
        addSeries(waitChart, "Systeme avec IA", result.ai, true);

        LineChart<Number, Number> throughputChart = createLineChart(
                "Evolution des vehicules traites",
                "Temps de simulation (s)",
                "Vehicules sortis"
        );
        addSeries(throughputChart, "Systeme classique", result.classic, false);
        addSeries(throughputChart, "Systeme avec IA", result.ai, false);

        HBox charts = new HBox(16, waitChart, throughputChart);
        charts.setPadding(new Insets(16));
        charts.setStyle("-fx-background-color: #0f172a;");
        HBox.setHgrow(waitChart, Priority.ALWAYS);
        HBox.setHgrow(throughputChart, Priority.ALWAYS);

        Stage stage = new Stage();
        stage.setTitle("Graphes comparatifs - Classique vs IA");
        stage.setScene(new Scene(charts, 1100, 520));
        stage.show();
    }

    private LineChart<Number, Number> createLineChart(String title, String xLabel, String yLabel) {
        NumberAxis xAxis = new NumberAxis();
        NumberAxis yAxis = new NumberAxis();
        xAxis.setLabel(xLabel);
        yAxis.setLabel(yLabel);

        LineChart<Number, Number> chart = new LineChart<>(xAxis, yAxis);
        chart.setTitle(title);
        chart.setAnimated(false);
        chart.setCreateSymbols(false);
        chart.setLegendVisible(true);
        chart.setMinWidth(520);
        return chart;
    }

    private void addSeries(LineChart<Number, Number> chart, String name,
                           java.util.List<SimulationBenchmark.DataPoint> points,
                           boolean waitMetric) {
        XYChart.Series<Number, Number> series = new XYChart.Series<>();
        series.setName(name);
        for (SimulationBenchmark.DataPoint point : points) {
            double value = waitMetric ? point.averageWaitSeconds : point.completedVehicles;
            series.getData().add(new XYChart.Data<>(point.timeSeconds, value));
        }
        chart.getData().add(series);
    }

    // ═══ UI UPDATE (each frame) ═══
    private void updateUI(long now) {
        frameCount++;
        if (now - lastFpsTime >= 1_000_000_000L) {
            fpsLabel.setText(String.valueOf(frameCount));
            frameCount = 0;
            lastFpsTime = now;
        }

        SimulationEngine engine = animationController.getSimulationEngine();
        carsLabel.setText(String.valueOf(animationController.getActiveCarsCount()));
        waitNSLabel.setText(String.format("%.1fs", engine.getAvgWaitNS()));
        waitEWLabel.setText(String.format("%.1fs", engine.getAvgWaitEW()));

        // Traffic light indicators
        updateLightIndicator(engine.getNsState(), nsRed, nsYellow, nsGreen);
        updateLightIndicator(engine.getEwState(), ewRed, ewYellow, ewGreen);

        // Emergency alert
        if (engine.getActiveEmergency() == null)
            emergencyAlert.setVisible(false);

        // AI panel
        if (engine.isAiMode() && engine.getAgent() != null) {
            QLearningAgent agent = engine.getAgent();
            aiScoreLabel.setText(String.format("Score: %.1f | Étapes: %d | ε: %.0f%% | États: %d",
                    agent.getTotalReward(), agent.getTotalSteps(), agent.getEpsilon() * 100, agent.getQTableSize()));
            aiDecisionLabel.setText(agent.getLastActionName());
            aiConfidenceLabel.setText(String.format("Confiance: %.0f%%", agent.getLastConfidence() * 100));
            aiReasonLabel.setText(agent.getLastDecisionReason());
        }

        // AI Log sync
        java.util.List<String> log = engine.getAiLog();
        if (aiLogBox.getChildren().size() != log.size()) {
            aiLogBox.getChildren().clear();
            for (String entry : log) {
                Label l = new Label(entry);
                l.setTextFill(entry.contains("🚨") ? Color.rgb(239, 68, 68) : Color.rgb(148, 163, 184));
                l.setFont(Font.font(9));
                l.setWrapText(true);
                aiLogBox.getChildren().add(l);
            }
        }
    }

    private void updateLightIndicator(TrafficLight.State state, Circle red, Circle yellow, Circle green) {
        red.setFill(Color.rgb(60, 20, 20));
        yellow.setFill(Color.rgb(60, 50, 0));
        green.setFill(Color.rgb(10, 60, 10));
        switch (state) {
            case RED:
                red.setFill(Color.RED);
                break;
            case YELLOW:
                yellow.setFill(Color.rgb(255, 200, 0));
                break;
            case GREEN:
                green.setFill(Color.LIMEGREEN);
                break;
            case RED_YELLOW:
                red.setFill(Color.RED);
                yellow.setFill(Color.rgb(255, 200, 0));
                break;
        }
    }

    // ═══ UI HELPERS ═══
    private String panelStyle() {
        return "-fx-background-color: rgba(15, 23, 42, 0.82); " +
                "-fx-background-radius: 18; " +
                "-fx-border-color: rgba(255, 255, 255, 0.08); " +
                "-fx-border-radius: 18; " +
                "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.4), 15, 0, 0, 0);";
    }

    private HBox wrapPanel(javafx.scene.Node content) {
        HBox box = new HBox(content);
        box.setStyle(panelStyle());
        box.setPadding(new Insets(10, 16, 10, 16));
        return box;
    }

    private Label statValue(String text) {
        Label l = new Label(text);
        l.setFont(Font.font("Consolas", FontWeight.BOLD, 18));
        l.setTextFill(Color.rgb(56, 189, 248));
        return l;
    }

    private VBox statBox(Label value, String label) {
        Label lbl = new Label(label);
        lbl.setFont(Font.font("System", 8));
        lbl.setTextFill(Color.rgb(100, 116, 139));
        VBox box = new VBox(2, value, lbl);
        box.setStyle("-fx-background-color: rgba(30,41,59,0.6); -fx-padding: 8; -fx-background-radius: 6;");
        box.setPrefWidth(110);
        return box;
    }

    private Circle light(Color color) {
        Circle c = new Circle(7, color);
        c.setStroke(Color.rgb(40, 40, 40));
        c.setStrokeWidth(1);
        return c;
    }

    private Button makeBtn(String text, String color) {
        Button btn = new Button(text);
        btn.setFocusTraversable(false);
        btn.setStyle("-fx-background-color: " + color
                + "; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 11px; -fx-padding: 6 14; -fx-background-radius: 6; -fx-cursor: hand;");
        return btn;
    }

    private Button makeToggleBtn(String text, boolean active) {
        Button btn = new Button(text);
        btn.setFocusTraversable(false);
        btn.setStyle(toggleStyle(active));
        return btn;
    }

    private String toggleStyle(boolean active) {
        return active
                ? "-fx-background-color: #0ea5e9; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 12px; -fx-padding: 8 18; -fx-background-radius: 6; -fx-cursor: hand;"
                : "-fx-background-color: transparent; -fx-text-fill: #94a3b8; -fx-font-size: 12px; -fx-padding: 8 18; -fx-background-radius: 6; -fx-cursor: hand;";
    }

    private Button makeScenarioBtn(String text, String bg, String border) {
        Button btn = new Button(text);
        btn.setFocusTraversable(false);
        btn.setStyle("-fx-background-color: " + bg
                + "; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 12px; -fx-padding: 10 20; -fx-background-radius: 10; -fx-cursor: hand; -fx-border-color: "
                + border + "; -fx-border-width: 1; -fx-border-radius: 10;");
        return btn;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
