package com.chatapp.client;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.Parent;
import javafx.stage.Stage;

public class MainClientApp extends Application {
    @Override
    public void start(Stage stage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("/client-view.fxml"));
        Scene scene = new Scene(root, 800, 500);
        scene.getStylesheets().add(getClass().getResource("/client-style.css").toExternalForm());
        stage.setTitle("TCP Client - Group Chat");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}