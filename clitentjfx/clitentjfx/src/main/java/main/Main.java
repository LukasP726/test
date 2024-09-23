package main;

import Controllers.StageController;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;

public class Main extends Application {

    public static int c = 0;

    @Override
    public void start(Stage primaryStage){
        primaryStage.setTitle("Brigit");
        primaryStage.setOnCloseRequest(windowEvent -> {
            Platform.exit();
            System.exit(0);
        });
        StageController stageController = new StageController(primaryStage);
        stageController.showBeginScene();
    }


    public static void main(String[] args) {
        launch(args);
    }
}
