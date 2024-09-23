package Controllers;

import Graphics.BeginScene;
import Graphics.BoardView;
import Graphics.LobbyScene;
import Net.Net;
import Parts.Log;
import Parts.RoomInfo;
import Parts.Alerts;
import javafx.application.Platform;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.net.Socket;
import java.util.ArrayList;

public class StageController {
    private final ObjectProperty<Stage> stage;
    private final IntegerProperty SETTINGS_MIN_HEIGHT = new SimpleIntegerProperty(620);
    private final IntegerProperty SETTING_MIN_WIGHT =  new SimpleIntegerProperty(400);
    private final IntegerProperty GAME_MIN_HEIGHT = new SimpleIntegerProperty(620);
    private final IntegerProperty GAME_MIN_WIGHT = new SimpleIntegerProperty(400);
    private final IntegerProperty SETTINGS_START_HEIGHT = new SimpleIntegerProperty(620);
    private final IntegerProperty SETTINGS_START_WIGHT =  new SimpleIntegerProperty(400);
    private final IntegerProperty GAME_START_HEIGHT = new SimpleIntegerProperty(620);
    private final IntegerProperty GAME_START_WIGHT =  new SimpleIntegerProperty(560);
    private BoardView board;
    private Net net;
    public LobbyScene lobbyScene;
    public LobbyController lobbyController;
    public BoardControl boardControl;
    public Log log;


    public StageController(Stage stage) {
        this.stage = new SimpleObjectProperty<Stage>(stage);
        this.log = new Log();
    }


    public void showBeginScene() {
        BeginScene beginScene = new BeginScene(this);
        this.stage.get().setScene(beginScene.getStartScene());
        this.stage.get().setMinWidth(300);
        this.stage.get().setMinHeight(150);
        this.stage.get().setWidth(300);
        this.stage.get().setHeight(150);
        this.stage.get().show();
    }

    public void createLobbyScene() {
        this.lobbyController = new LobbyController(this);
        this.lobbyScene = new LobbyScene(lobbyController);
        this.stage.get().setScene(this.lobbyScene.createScene());
        this.stage.get().setMinWidth(this.SETTING_MIN_WIGHT.get());
        this.stage.get().setMinHeight(this.SETTINGS_MIN_HEIGHT.get());
        this.stage.get().setHeight(this.SETTINGS_START_HEIGHT.get());
        this.stage.get().setWidth(this.SETTINGS_START_WIGHT.get());

    }

    public void showLobbyScene(){
        this.stage.get().show();
    }

    public void showGameScene(BoardView board) {
        this.board = board;
        this.stage.get().setScene(board.createBoardGameScene());
        this.stage.get().setMinWidth(this.GAME_MIN_WIGHT.get());
        this.stage.get().setMinHeight(this.GAME_MIN_HEIGHT.get());
        this.stage.get().setWidth(this.GAME_START_WIGHT.get());
        this.stage.get().setHeight(this.GAME_START_HEIGHT.get());
        this.stage.get().show();
    }


    /***
     * gets clients name
     * @return given clients name
     */
    public String getName(){
        return this.lobbyScene.getGivenName();
    }


    /****
     * does move server - gui
     * @param x x-axis
     * @param y y-axis
     */
    public void doMove(int x, int y) {
        this.board.setButtonsOnBoard(x, y);
        System.out.println("doing move: [" + x + ";" + y +"]");
    }


    /***
     * creates working lobby with communicant in form of Net
     * @param text
     */
    public void createLobby(String text) {
        this.net = new Net(text, this);
        if (this.net.isSocket_ready()){
            //TODO na tohle se koukni
            this.net.changeNotInGame();
        }

    }

    public int getSettingMinWight() {
        return SETTING_MIN_WIGHT.get();
    }

    public int getSettingMinHeight() {
        return SETTINGS_MIN_HEIGHT.get();
    }


    /********
     * called by refresh button, updates rooms manually
     */
    public void updateRoomList() {
        this.net.updateRoomList();
    }

    /********
     * when refresh comes...
     * @param rooms
     */
    public void refreshRoomList(ArrayList<RoomInfo> rooms) {
        if (this.lobbyScene != null)
        Platform.runLater(() ->this.lobbyScene.updateRooms(rooms));
    }

    /***
     * sends create order to server
     * @param name players name
     */
    public void createRoom(String name) {
        String message = "CREATE|" + name + "\n";
        this.net.writeMessage(message);
    }

    /****
     * asks server if joining game is possible
     * @param room room to connect to
     * @param name clients name
     */
    public void sendGameStartQuestion(RoomInfo room, String name) {
        this.net.writeMessage("JOIN|" + room.getID() + "|" + name + "\n");
    }

    /****
     * returns player to lobby
     */
    public void backToLobby(){
        String name = this.lobbyScene.givenName;
        this.stage.get().setScene(this.lobbyScene.createScene());
        this.lobbyScene.setTFName(name);
        this.stage.get().show();
    }

    /****
     * loads game player disconnected from
     * @param gameState state of game
     * @param playRoom room to connect to
     */
    public void loadGame(String []gameState, RoomInfo playRoom){
        startGame(playRoom);
        System.out.println("loading game");
        int positionInString = 4;
        for (int i = 0; i < 11; i++) {
            for (int j = 0; j < 11; j++) {
                if (Integer.parseInt(gameState[positionInString]) != 0)
                    this.board.colorButton(i, j, Integer.parseInt(gameState[positionInString]));
                positionInString++;
            }
        }
        //druhý hráč je 0 problabla
        this.board.setPlayer(!gameState[positionInString].equals("0"));
        System.out.println("Game loaded");
    }

    /****
     * starts a game
     * @param playRoom room in whitch g´the game happens
     * @return board controller
     */
    public BoardControl startGame(RoomInfo playRoom) {
        System.out.println("----------START_GAME: STAGE_CTRL");
        System.out.println("pl1_name: " + playRoom.getplayer(0).getUsername());
        System.out.println("pl2Name: " + playRoom.getplayer(1).getUsername());
        startMainGame(playRoom.getplayer(0).getUsername(), playRoom.getplayer(1).getUsername(), this.net);
        System.out.println("Game_started");
        return boardControl;
    }

    /*****
     * starts main game
     * @param namePl1 player name
     * @param namePl2 player 2 name
     * @param net net
     */
    public void startMainGame(String namePl1, String namePl2, Net net) {
        System.out.println("startMainGame - settingController");
        this.boardControl = new BoardControl(namePl1, namePl2, 5, Color.RED, Color.BLUE, net);
        this.board = new BoardView(boardControl, this);
        this.showGameScene(board);
    }

    /****
     * redoes net with all old info (viz net)
     * @param address
     * @param controller
     * @param not_in_game
     * @param playRoom
     * @param boardController
     * @param firstPlayer
     * @param thisPlayersName
     * @param roomID
     * @param inGame
     * @param socket
     */
    public void redoNet(String address, StageController controller, boolean not_in_game, RoomInfo playRoom, BoardControl boardController, boolean firstPlayer, String thisPlayersName, String roomID, boolean inGame, Socket socket){
        this.net = new Net(address, this, not_in_game, playRoom, boardController, firstPlayer, thisPlayersName, roomID, inGame, socket);
        this.board.boardControl.setNet(this.net);
    }

    /****
     * writes message to server
     * @param s
     */
    public void writeMessage(String s) {
        System.out.println("Writting message: " + s);
        this.net.writeMessage(s);
    }

    /****
     * writes turn done by player to server
     * @param ii x-axis
     * @param jj y-axis
     */
    public void writeTurnMessage(int ii, int jj) {
        writeMessage("TURN|" + this.net.getPlayRoom().getID() + "|" + getName() + "|" + ii + "|" + jj + "\n");
    }

    public void room_created(){
        this.lobbyScene.newGame.setDisable(true);
        this.lobbyScene.refreshBT.setDisable(true);
        this.lobbyScene.roomListView.setDisable(true);
        this.lobbyScene.namePl1TF.setDisable(true);
        Platform.runLater(Alerts::roomCreated);
    }
}