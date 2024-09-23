package Graphics;

import Controllers.BoardControl;
import Controllers.LobbyController;
import Controllers.StageController;
import Parts.Alerts;
import Parts.GameBoardButton;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

public class BoardView {
    private StageController stageController;
    public BoardControl boardControl;
    private GameBoardButton[][] board;
    private Label name;

    /*****
     * board constructor
     * @param boardControl board controller
     * @param stageController stagecontroller
     */
    public BoardView(BoardControl boardControl, StageController stageController){
        this.stageController = stageController;
        this.boardControl = boardControl;

    }


    public Scene createBoardGameScene() {
        Scene gameScene = new Scene(createBoardControlScene());
        try{
            gameScene.getStylesheets().add("gameBoard.css");
        }
        catch(Exception ignored){}
        return gameScene;
    }

    private Parent createBoardControlScene() {
        BorderPane rootPane = new BorderPane();
        rootPane.setTop(topMenu());
        rootPane.setCenter(gameboard());
        return rootPane;
    }

    private Node topMenu() {
        MenuBar bar = new MenuBar();

        Menu help = new Menu("Help");

        MenuItem info = new MenuItem("About");
        info.setOnAction(actionEvent -> Alerts.showAbout());
        MenuItem rules = new MenuItem("Help");
        rules.setOnAction(actionEvent -> Alerts.getHelpWindow(false));
        help.getItems().addAll(info, rules);
        bar.getMenus().addAll(help);

        return bar;
    }

    private Node gameboard() {
        VBox out = new VBox(10);
        out.setPadding(new Insets(10));
        out.setAlignment(Pos.CENTER);
        this.name = new Label();
        name.setId("name");
        name.setText(this.boardControl.get1stPlrName());
        name.setTextFill(this.boardControl.getPlayer1Color());
        GridPane gameBoard = new GridPane();
        gameBoard.setId("board");
        gameBoard.setVgap(2);
        gameBoard.setHgap(2);
        gameBoard.setPadding(new Insets(20));


        this.board = new GameBoardButton[this.boardControl.getSize()][this.boardControl.getSize()];
        //hodnota pro indexovani tlacitek
        int index = 0;
        for (int i = 0; i < this.boardControl.getSize(); i++) {
            for (int j = 0; j < this.boardControl.getSize(); j++) {
                board[i][j] = new GameBoardButton(index);
                index++;
                gameBoard.add(board[i][j], i, j);
                if ((i + j) % 2 != 0) {
                    int ii = i;
                    int jj = j;
                    if (ii % 2 == 0){
                        //1st player
                        boolean plOneButton = true;
                        String some = boardControl.getPlayer1Color().toString();
                        board[ii][jj].setStyle("-fx-border-color: #"+ some.substring(2));
                        board[ii][jj].setOnAction(event -> {
                            boardControl.comamndantControlle(ii, jj, plOneButton);
                        });
                    }
                    else
                    {
                        boolean plOneButton = false;
                        String some = boardControl.getPlayer2Color().toString();
                        board[ii][jj].setStyle("-fx-border-color: #"+ some.substring(2));
                        board[ii][jj].setOnAction(event -> {
                            boardControl.comamndantControlle(ii, jj, plOneButton);
                        });
                    }

                }
                else{
                    board[i][j].setDisable(true);
                    board[i][j].setId("offButton");
                }
            }
        }
        gameBoard.setAlignment(Pos.CENTER);
        out.getChildren().addAll(name, gameBoard);
        boardControl.deliverBGInfo(board, this.stageController);
        return out;
    }

    /****
     * method for coloring of buttons via server means
     * @param i x-axis
     * @param j y-axis
     * @param player player
     */
    public void colorButton(int i, int j, int player){
        System.out.println( "[" + i + ";" + j + "]");
        if(player != 0){
            if (player == 1){
                board[i][j].setStyle("-fx-background-color: #"+ this.boardControl.getPlayer1Color().toString().substring(2));
                board[i][j].setColor(this.boardControl.getPlayer1Color());
            }
            else {
                board[i][j].setStyle("-fx-background-color: #"+ this.boardControl.getPlayer2Color().toString().substring(2));
                board[i][j].setColor(this.boardControl.getPlayer2Color());
            }
        }
        else{

        }
    }

    /****
     * colors clicked button + surrounding buttons if needed
     * @param ii x-axis
     * @param jj y-axis
     */
    public void setButtonsOnBoard( int ii, int jj) {
        //TODO kdyby to nefungovalo, pošli 'String some' jako barvu hráče a místo this.bc.getactcol.tostr dej 'some'
        board[ii][jj].setStyle("-fx-background-color: #"+ this.boardControl.getActiveColor().toString().substring(2));
        board[ii][jj].setColor(this.boardControl.getActiveColor());
        if (ii + 2 < this.board.length) {
            if (board[ii + 2][jj].getButtonColor().equals(board[ii][jj].getButtonColor()) && board[ii + 1][jj].getButtonColor().equals(Color.WHITE)) {
                board[ii + 1][jj].setStyle("-fx-background-color: #" + this.boardControl.getActiveColor().toString().substring(2));
                board[ii + 1][jj].setColor(this.boardControl.getActiveColor());
            }
        }

        if (jj + 2 < this.board.length) {
            if (board[ii][jj + 2].getButtonColor().equals(board[ii][jj].getButtonColor()) && board[ii][jj + 1].getButtonColor().equals(Color.WHITE)) {
                board[ii][jj + 1].setStyle("-fx-background-color: #" + this.boardControl.getActiveColor().toString().substring(2));
                board[ii][jj + 1].setColor(this.boardControl.getActiveColor());
            }
        }
        if (ii - 2 >= 0) {
            if (board[ii - 2][jj].getButtonColor().equals(board[ii][jj].getButtonColor()) && board[ii - 1][jj].getButtonColor().equals(Color.WHITE)) {
                board[ii - 1][jj].setStyle("-fx-background-color: #" + this.boardControl.getActiveColor().toString().substring(2));
                board[ii - 1][jj].setColor(this.boardControl.getActiveColor());
            }
        }

        if (jj - 2 >= 0) {
            if (board[ii][jj - 2].getButtonColor().equals(board[ii][jj].getButtonColor()) && board[ii][jj - 1].getButtonColor().equals(Color.WHITE)) {
                board[ii][jj - 1].setStyle("-fx-background-color: #" + this.boardControl.getActiveColor().toString().substring(2));
                board[ii][jj - 1].setColor(this.boardControl.getActiveColor());
            }
        }
        this.boardControl.nextPlayersTurn(this.name);
    }

    /****
     * sets active player
     * @param activePlayer ...
     */
    public void setPlayer(boolean activePlayer) {
        //TODO - jestli tohle nebude fungovat........
        if (this.boardControl.getActivePlayer() == activePlayer){return;}
        else{
            this.boardControl.nextPlayersTurn(this.name);
        }

    }
}
