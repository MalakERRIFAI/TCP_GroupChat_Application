package com.chatapp.server;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.Parent;
import javafx.stage.Stage;

public class MainServerApp extends Application {
    @Override
    public void start(Stage stage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("/server-view.fxml"));
        Scene scene = new Scene(root, 900, 500);
        scene.getStylesheets().add(getClass().getResource("/server-style.css").toExternalForm());
        stage.setTitle("TCP Server - Group Chat");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}