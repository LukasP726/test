package Controllers;

import Net.Net;
import Parts.Alerts;
import Parts.GameBoardButton;
import javafx.application.Platform;
import javafx.beans.property.*;
import javafx.scene.control.Label;
import javafx.scene.paint.Color;



public class BoardControl {
    private final StringProperty name1;
    private final StringProperty name2;

    private final IntegerProperty buttonNum;
    private final IntegerProperty size;
    private final ObjectProperty<Color> player1Color;
    private final ObjectProperty<Color> player2Color;
    private final ObjectProperty<Color> activeColor;
    private final BooleanProperty activePlayer;
    private final StringProperty activePlayerName;
    private Net net;

    private GameBoardButton[][] board;
    private StageController stageControl;

    /****
     * board controller constructor
     * @param pl1Name pl1name
     * @param pl2Name pl2
     * @param buttonNum num of buttons
     * @param player1Color player1color
     * @param player2Color player1+1color
     * @param net net working class
     */
    public BoardControl(String pl1Name, String pl2Name, int buttonNum, Color player1Color, Color player2Color, Net net){
        this.name1 = new SimpleStringProperty(pl1Name);
        this.name2 = new SimpleStringProperty(pl2Name);
        this.buttonNum = new SimpleIntegerProperty(buttonNum);
        this.size = new SimpleIntegerProperty(this.buttonNum.get() * 2 + 1);
        //true pro prvniho hrace
        this.activePlayer = new SimpleBooleanProperty(true);
        this.activePlayerName = new SimpleStringProperty(pl1Name);
        this.player1Color = new SimpleObjectProperty<Color>(player1Color);
        this.player2Color = new SimpleObjectProperty<Color>(player2Color);
        this.activeColor = new SimpleObjectProperty<Color>(this.player1Color.get());
        //TODO - vytvoř statistics a kontrolu vyhry ad a moznosti vytvoreni cesty na serveru!!!
        System.out.println("BUILDED BOARD");
        this.net = net;
    }

    /***
     * actually setter for...
     * @param board gameboard
     * @param control stagecontroller
     */
    public void deliverBGInfo(GameBoardButton[][] board, StageController control) {
        this.board = board;
        this.stageControl = control;
    }


    /****
     * changes active player to the other one
     * @param name name of newly active player
     */
    public void nextPlayersTurn(Label name){
        this.activePlayer.setValue(!this.activePlayer.get());
        if (this.activePlayer.get()) {
            System.out.println("changing player from \'" + this.get2ndPlrName() + "\' to \'" + this.get1stPlrName());
            this.activeColor.setValue(this.player1Color.get());
            this.activePlayerName.setValue(this.get1stPlrName());
            name.setText(this.activePlayerName.getValue());
            name.setTextFill(this.activeColor.get());
        }
        else {
            System.out.println("changing player from \'" + this.get1stPlrName() + "\' to \'" + this.get2ndPlrName());
            this.activeColor.setValue(this.player2Color.get());
            this.activePlayerName.setValue(this.get2ndPlrName());
            name.setText(this.activePlayerName.getValue());
            name.setTextFill(this.activeColor.getValue());
        }
    }



    public int getSize() {
        return size.get();
    }

    public String get1stPlrName(){
        return name1.get();
    }

    public String get2ndPlrName() {
        return name2.get();
    }

    public Color getPlayer1Color() {
        return player1Color.get();
    }

    public Color getPlayer2Color() {
        return player2Color.get();
    }

    public boolean getActivePlayer(){
        return this.activePlayer.getValue();
    }

    public Color getActiveColor(){
        return this.activeColor.get();
    }

    public void setNet(Net net) {
        this.net = net;
    }

    /****
     * sends move to server
     * @param ii x-axis
     * @param jj y-axis
     * @param plOneButton whos button is it?
     */
    public void comamndantControlle(int ii, int jj, boolean plOneButton) {
        if ((this.activePlayer.getValue() && plOneButton) || (!this.activePlayer.getValue() && !plOneButton)){
            System.out.println("tah na souřadnicích [" + ii + ";" + jj + "]");
            this.stageControl.writeTurnMessage(ii, jj);
        }
    }

    /****
     * commences vicotry
     * @param winner winning player
     */
    public void commenceVictory(String winner) {
        //todo zablokuj všchna pole na view
        for (GameBoardButton[] buttons: board) {
            for (GameBoardButton button: buttons) {
                button.setDisable(true);
            }
        }
        Platform.runLater(() -> {
            if(Alerts.showWinScene(winner)){
                this.stageControl.backToLobby();
            }
            else {
                System.out.println("LOBBY NOT OPENED!!! HOW?????");
                System.exit(-1000);
            }
            //
        });
    }
}
