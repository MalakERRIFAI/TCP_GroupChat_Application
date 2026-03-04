package com.chatapp.client.ui;

import com.chatapp.client.config.AppConfig;
import com.chatapp.client.model.ChatClient;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;

public class ClientController {

    @FXML private VBox loginPane;
    @FXML private BorderPane chatPane;

    @FXML private TextField usernameField;
    @FXML private Label loginInfoLabel;

    @FXML private Label statusLabel;
    @FXML private Circle statusDot;

    @FXML private TextArea chatArea;
    @FXML private TextField messageField;
    @FXML private Button sendBtn;

    private final AppConfig config = new AppConfig();
    private final ChatClient client = new ChatClient();

    private boolean readOnly = false;

    @FXML
    public void initialize() {
        setOnline(false);

        // Enter key sends message
        messageField.setOnAction(e -> onSend());

        client.setOnOnline(this::setOnline);
        client.setOnMessage(this::handleServerLine);
    }

    @FXML
    public void onJoin() {
        String username = usernameField.getText() == null ? "" : usernameField.getText().trim();
        readOnly = username.isBlank();

        try {
            client.connect(config.ip(), config.port());
            client.setUsername(username);

            loginPane.setVisible(false);
            loginPane.setManaged(false);

            chatPane.setVisible(true);
            chatPane.setManaged(true);

            if (readOnly) {
                chatArea.appendText("[SYSTEM] READ-ONLY MODE: you can read messages but not send.\n");
                sendBtn.setDisable(true);
                messageField.setDisable(true);
            } else {
                sendBtn.setDisable(false);
                messageField.setDisable(false);
            }

        } catch (Exception ex) {
            loginInfoLabel.setText("Connection failed: " + ex.getMessage());
        }
    }

    @FXML
    public void onSend() {
        if (readOnly) return;

        String msg = messageField.getText();
        if (msg == null) return;

        msg = msg.trim();
        if (msg.isEmpty()) return;

        // end/bye disconnect
        if (msg.equalsIgnoreCase("end") || msg.equalsIgnoreCase("bye")) {
            onDisconnect();
            return;
        }

        if (msg.equalsIgnoreCase("allUsers")) {
            client.requestAllUsers();
            messageField.clear();
            return;
        }

        client.sendMessage(msg);
        messageField.clear();
    }

    @FXML
    public void onAllUsers() {
        client.requestAllUsers();
    }

    @FXML
    public void onDisconnect() {
        client.quit();
        Platform.exit();
    }

    private void handleServerLine(String line) {
        Platform.runLater(() -> {
            if (line.startsWith("SYS:")) {
                chatArea.appendText("[SYSTEM] " + line.substring(4) + "\n");
            } else if (line.startsWith("CHAT:")) {
                // CHAT:time|user|msg
                String payload = line.substring(5);
                String[] parts = payload.split("\\|", 3);
                if (parts.length == 3) {
                    chatArea.appendText("[" + parts[0] + "] " + parts[1] + ": " + parts[2] + "\n");
                } else {
                    chatArea.appendText(payload + "\n");
                }
            } else if (line.startsWith("USERS:")) {
                String users = line.substring(6);
                chatArea.appendText("[USERS] " + (users.isBlank() ? "(none)" : users) + "\n");
            } else {
                chatArea.appendText(line + "\n");
            }
        });
    }

    private void setOnline(boolean online) {
        Platform.runLater(() -> {
            statusLabel.setText(online ? "Online" : "Offline");
            statusDot.setStyle(online
                    ? "-fx-fill: #2ecc71;"   // green
                    : "-fx-fill: #e74c3c;"   // red
            );
        });
    }
}