package com.chatapp.server.ui;

import com.chatapp.server.config.AppConfig;
import com.chatapp.server.model.ChatServer;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.paint.Color;

import java.util.*;

public class ServerController {

    @FXML private Button startBtn;
    @FXML private Button stopBtn;
    @FXML private Label portLabel;
    @FXML private TextArea logArea;
    @FXML private ListView<String> usersList;

    private ChatServer server;
    private final AppConfig config = new AppConfig();

    // store random color for each user
    private final Map<String, String> userColor = new HashMap<>();

    @FXML
    public void initialize() {
        portLabel.setText(String.valueOf(config.port()));

        usersList.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(String user, boolean empty) {
                super.updateItem(user, empty);
                if (empty || user == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(user);
                    String hex = userColor.getOrDefault(user, "#dddddd");
                    setStyle("-fx-background-color: " + hex + "; -fx-text-fill: black;");
                }
            }
        });
    }

    @FXML
    public void onStart() {
        server = new ChatServer(config.port());
        server.setLogSink(this::uiLog);
        server.setUsersSink(users -> Platform.runLater(() -> {
            for (String u : users) userColor.putIfAbsent(u, randomSoftColorHex());
            usersList.getItems().setAll(users);
        }));

        server.start();
        startBtn.setDisable(true);
        stopBtn.setDisable(false);
    }

    @FXML
    public void onStop() {
        if (server != null) server.stop();
        startBtn.setDisable(false);
        stopBtn.setDisable(true);
        usersList.getItems().clear();
        uiLog("Server stopped by user.");
    }

    private void uiLog(String msg) {
        Platform.runLater(() -> logArea.appendText(msg + "\n"));
    }

    private String randomSoftColorHex() {
        // Pick a random hue (0..360), keep saturation + brightness high enough to be distinct but still pastel
        double h = Math.random() * 360.0;
        double s = 0.55; // saturation (0..1)
        double v = 0.92; // brightness (0..1)

        return hsvToHex(h, s, v);
    }

    private String hsvToHex(double h, double s, double v) {
        double c = v * s;
        double x = c * (1 - Math.abs(((h / 60.0) % 2) - 1));
        double m = v - c;

        double r1=0, g1=0, b1=0;
        if (0 <= h && h < 60)      { r1=c; g1=x; b1=0; }
        else if (60 <= h && h < 120) { r1=x; g1=c; b1=0; }
        else if (120 <= h && h < 180){ r1=0; g1=c; b1=x; }
        else if (180 <= h && h < 240){ r1=0; g1=x; b1=c; }
        else if (240 <= h && h < 300){ r1=x; g1=0; b1=c; }
        else                        { r1=c; g1=0; b1=x; }

        int r = (int)Math.round((r1 + m) * 255);
        int g = (int)Math.round((g1 + m) * 255);
        int b = (int)Math.round((b1 + m) * 255);

        return String.format("#%02X%02X%02X", r, g, b);
    }
}