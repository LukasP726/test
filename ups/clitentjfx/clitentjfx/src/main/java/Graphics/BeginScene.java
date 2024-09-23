package Graphics;

import Controllers.StageController;
import Parts.Alerts;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class BeginScene {
    private StageController stageController;
    private TextField address;

    /****
     * begin scene contructor
     * @param stageController stagecontroller
     */
    public BeginScene(StageController stageController){
        this.stageController = stageController;
    }

    /***
     * starts scene
     */
    public Scene getStartScene() {
        Scene somethingTerrible = new Scene(createInsides(), 800, 300);
        try{
            somethingTerrible.getStylesheets().add("application.css");  //navázání css souboru, nutno na instanci z classy Scene
            //somethingTerrible.getStylesheets().add("application.css");
        }
        catch(Exception ignored){
        }
        return somethingTerrible;
    }

    private Parent createInsides() {

        VBox parts = new VBox(15);
        parts.setAlignment(Pos.CENTER);
        HBox something_more_terrible = new HBox(10);
        something_more_terrible.setAlignment(Pos.CENTER);
        Label addressLabel = new Label("Address:");
        addressLabel.setId("pl2Text");
        this.address = new TextField();
        this.address.setText("127.0.0.1:1234");
        something_more_terrible.getChildren().addAll(addressLabel, this.address);

        HBox something_even_worse = new HBox(10);
        something_even_worse.setAlignment(Pos.BOTTOM_CENTER);
        Button startBT = new Button("Connect");
        startBT.setOnAction(event -> {
            if (address.getText() != null && address.getText().matches("^(([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])\\.){3}([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5]):[0-9]+$")){
                this.stageController.createLobby(this.address.getText());
            }
            else {
                Alerts.fotmatError();

            }
        });

        Button exitBT = new Button("Quit");
        exitBT.setOnAction(event ->  Alerts.exit(this.stageController.log));
        something_even_worse.getChildren().addAll(startBT, exitBT);
        parts.getChildren().addAll(something_more_terrible, something_even_worse);
        return parts;
    }

}
