package org.example;

import Controllers.StageController;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;


/**
 * JavaFX App
 */
public class App extends Application {

    @Override
    public void start(Stage stage) {
        var javaVersion = SystemInfo.javaVersion();
        var javafxVersion = SystemInfo.javafxVersion();

        //var label = new Label("Hello, JavaFX " + javafxVersion + ", running on Java " + javaVersion + ".");
        //var scene = new Scene(new StackPane(label), 640, 480);
        //stage.setScene(scene);
        //stage.show();
        stage.setTitle("Brigit");
        stage.setOnCloseRequest(windowEvent -> {
            Platform.exit();
            System.exit(0);
        });
        StageController stageController = new StageController(stage);
        stageController.showBeginScene();
    }

    public static void main(String[] args) {
        launch();
    }

}