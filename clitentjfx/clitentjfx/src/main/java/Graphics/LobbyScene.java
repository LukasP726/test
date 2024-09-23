package Graphics;

import Controllers.LobbyController;
import Parts.Alerts;
import Parts.RoomInfo;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import java.util.ArrayList;

public class LobbyScene {
    private Scene settingScene;
    private LobbyController lobbyController;
    private Text pl1Text;
    public TextField namePl1TF;
    public ListView<RoomInfo> roomListView;
    private ArrayList<RoomInfo> roomList;
    public String givenName = "Player1";
    public Button newGame;
    public Button refreshBT;


    /***
     * creates lobby scene
     * @param lobbyController olobbycontroller
     */
    public LobbyScene(LobbyController lobbyController) {
        this.lobbyController = lobbyController;
        this.roomList = new ArrayList<>();
    }

    public Scene createScene(){
        this.settingScene = new Scene(createWindowsInsides(), lobbyController.getMinWeight(), lobbyController.getMinHeight());
        try {
            this.settingScene.getStylesheets().add("application.css");  //navázání css souboru, nutno na instanci z classy Scene
        }
        catch(Exception ignored){}
        return this.settingScene;
    }

    private Parent createWindowsInsides() {
        VBox mainBox = new VBox();
        mainBox.setPadding(new Insets(0, 0, 20,0 ));
        mainBox.setBackground(new Background(new BackgroundFill(Color.LIGHTBLUE, new CornerRadii(0), new Insets(0))));


        TilePane bot = createButtons();
        Label winName = new Label("BRIGIT");
        winName.setId("winName");
        mainBox.setAlignment(Pos.TOP_CENTER);
        bot.setPadding(new Insets(15));
        TilePane midPart = midParts();
        ListView<RoomInfo> listView = getRoomList();
        bot.setAlignment(Pos.BOTTOM_CENTER);
        midPart.setAlignment(Pos.CENTER);
        Label roomLabel = new Label("");
        mainBox.getChildren().addAll(winName, roomLabel, midPart, listView, bot);
        return mainBox;
    }

    private TilePane createButtons() {
        TilePane buttonsBox = new TilePane();
        buttonsBox.setId("buttonsBox");
        buttonsBox.setPrefColumns(3);
        buttonsBox.setPrefRows(3);
        Button exitBT = new Button("Quit Game");
        exitBT.setOnAction(event -> Alerts.exit(this.lobbyController.stageController.log));
        refreshBT = new Button("Refresh");
        refreshBT.setOnAction(event -> {
            //TODO - pošli zprávu na server, že chceš refresh, čuráku!!!
            lobbyController.updateRoomList();
        });

        this.newGame = new Button("Create room");
        newGame.setOnAction(event -> {
            System.out.println("this player name: " + this.namePl1TF.getText());
            this.lobbyController.createRoom(this.namePl1TF.getText());
        });

        buttonsBox.getChildren().addAll(newGame, refreshBT, exitBT);

        buttonsBox.setTileAlignment(Pos.BOTTOM_CENTER);
        return buttonsBox;

    }

    private TilePane midParts(){
        TilePane testPane = new TilePane();
        GridPane paneOne = new GridPane();

        paneOne.setId("paneOne");

        testPane.setId("tilePane");
        testPane.setPrefColumns(2);
        testPane.setPrefRows(2);

        this.pl1Text = new Text("Player Name:");
        pl1Text.setId("pl1Text");

        //todo bindni obsah TF s property v controlleru
        this.namePl1TF = new TextField("PlayerName");


        paneOne.add(pl1Text, 0,1);
        paneOne.add(namePl1TF, 1, 1);
        testPane.getChildren().add(paneOne);
        return testPane;

    }

    private ListView<RoomInfo> getRoomList() {
        if (this.roomList != null){
            System.out.println("arrayList: ");

            for (int i = 0; i < roomList.size(); i++) {
                System.out.println(this.roomList.get(i).toString());
            }
        }
        //TODO WTF??? proč to musí bejt takhle????
        RoomInfo[] selectedRoom = new RoomInfo[1];

        this.roomListView = new ListView<>(FXCollections.observableArrayList(this.roomList));
        this.roomListView.setOnMouseClicked(new EventHandler<MouseEvent>() {

            @Override
            public void handle(MouseEvent event) {
                System.out.println("clicked on " + roomListView.getSelectionModel().getSelectedItem());
                selectedRoom[0] = roomListView.getSelectionModel().getSelectedItem();
                if (roomListView.getSelectionModel().getSelectedItem() != null) {
                    if (Alerts.roomWindow(roomListView.getSelectionModel().getSelectedItem())) {
                        givenName = namePl1TF.getText();
                        lobbyController.sendGameStartQuestion(selectedRoom[0], namePl1TF.getText());
                    }
                }

            }
        });

        return this.roomListView;
    }

    /***
     * gets name given to player and saves it
     * TODO - and controls it!!!
     * @return
     */
    public String getGivenName(){
        this.givenName = this.namePl1TF.getText();
        return this.givenName;
    }

    //TODO - kdyžtak oprav...
    /*****
     * updates rooms in list
     * @param rooms rooms list
     */
    public void updateRooms(ArrayList<RoomInfo> rooms) {
        this.roomListView.getItems().clear();
        for (int i = 0; i < rooms.size(); i++) {
            this.roomListView.getItems().add(rooms.get(i));
        }
    }

    /****
     * sets players name in textfield
     * @param name players name
     */
    public void setTFName(String name){
        this.namePl1TF.setText(name);
    }


    }