package Parts;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DialogPane;

public class Alerts {
    /********
     * alert ukazujici 'about' informaci
     */
    public static void showAbout(){
        Alert info = new Alert(Alert.AlertType.INFORMATION);
        info.setTitle("About");
        info.setHeaderText("Game Brigit");
        info.setContentText(InGameTexts.getAboutText());
        info.showAndWait();
    }

    /********
     * alert ukazujici napovedu
     * @param settingsOrGameHelp napoveda pro nastaveni nebo hru
     */
    public static void getHelpWindow(boolean settingsOrGameHelp){
        Alert info = new Alert(Alert.AlertType.INFORMATION);
        info.setTitle("Help");
        info.setHeaderText("Game Brigit");
        if(settingsOrGameHelp) {
            info.setContentText(InGameTexts.getSettingsHelpText());
        }
        else {
            info.setContentText(InGameTexts.getGameHelpText());
        }
        info.showAndWait();

    }


    /********
     * alert pro potvrzeni ukonceni hry
     */
    public static void exit(Log log) {
        Alert a = new Alert(Alert.AlertType.CONFIRMATION);
        DialogPane pane = a.getDialogPane();
        for (ButtonType t : a.getButtonTypes())
            ((Button) pane.lookupButton(t)).setDefaultButton(t == ButtonType.CANCEL);
        a.setTitle("Exit");
        a.setHeaderText("Do you really want to exit the game?");
        a.showAndWait();
        if (a.getResult().equals(ButtonType.OK)) {
            Platform.exit();
            log.close();
            System.exit(0);
        }
    }

    /********
     * alert pro ukonceni programu s oznamenim, proc
     */
    public static void focedExit(String reason, int exitStatus, Log log) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        DialogPane pane = a.getDialogPane();
        for (ButtonType t : a.getButtonTypes())
            ((Button) pane.lookupButton(t)).setDefaultButton(t == ButtonType.OK);
        a.setTitle("Exit");
        a.setHeaderText(reason);
        a.showAndWait();
        if (a.getResult().equals(ButtonType.OK)) {
            Platform.exit();
            log.close();
            System.exit(exitStatus);
        }
    }

    /*****
     * common error alert
     * @param s error reason text
     */
    public static void commonError(String s, Log log) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setTitle("ERROR");
        a.setHeaderText("Could not reconnect: " + s);
        a.showAndWait();
        if (a.getResult().equals(ButtonType.OK)) {
            Platform.exit();
            log.close();
            System.exit(-2);
        }
    }

    /*****
     * room created info alert
     */
    public static void roomCreated() {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle("ROOM");
        a.setHeaderText("Room created, waiting for other player...");
        a.showAndWait();
    }

    /******
     * room info button, shows info about room and gives choice to connect or not
     * @param selectedItem room
     * @return true - connect to this rooom; false - return to lobby
     */
    public static boolean roomWindow(RoomInfo selectedItem) {
        ButtonType connect = new ButtonType("Connect");
        Alert a = new Alert(Alert.AlertType.CONFIRMATION);
        DialogPane pane = a.getDialogPane();
        a.setTitle("ROOM");
        a.setHeaderText(selectedItem.toString());
        a.setContentText(selectedItem.getPlayersInfo());
        a.getButtonTypes().remove(0);
        a.getButtonTypes().remove(0);
        a.getButtonTypes().add(0, connect);
        a.getButtonTypes().add(1, ButtonType.CANCEL);

        a.showAndWait();
        if (a.getResult().equals(connect)) {
            return true;
        }
        if (a.getResult().equals(ButtonType.CANCEL)) {
            return false;
        }
        return false;
    }

    /*****
     * alert when planned turn is invalid
     * @param s reason, why turn invald
     */
    public static void invalidTurnAlert(String s) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setTitle("ERROR");
        a.setHeaderText("TURN INVALID: " + s);
        a.showAndWait();

    }

    /****
     * shows win scene - its in the name...
     * @param winner winning palyers name
     * @return true when turned off
     */
    public static boolean showWinScene( String winner) {
        ButtonType ok = new ButtonType("Wow");
        Alert a = new Alert(Alert.AlertType.CONFIRMATION);
        a.setTitle("VICTORY");
        a.setHeaderText("PLAYER " + winner + " WINS!");
        a.getButtonTypes().clear();
        a.getButtonTypes().addAll(ok);
        a.showAndWait();
        if (a.getResult().equals(a.getButtonTypes().get(0))) {
            return true;
        }
        return true;
    }

    /*****
     * starter error, when wrong address format
     */
    public static void fotmatError(){
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setTitle("ERROR");
        a.setHeaderText("ERROR - ADDRESS IN WRONG FORMAT");
        a.showAndWait();
    }

    /*****
     * disconnected info alert
     */
    public static void disconnected() {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle("ERROR");
        a.setHeaderText("You disconnected......");
        a.showAndWait();
    }

    /*****
     * oponent off info alert
     */
    public static void oponentOff() {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle("ERROR");
        a.setHeaderText("Your opponent disconnected\nGoing back to lobby");
        a.showAndWait();
    }

    /*****
     * oponent disconected info alert
     */
    public static void opDisconnected() {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle("ERROR");
        a.setHeaderText("Your oponent disconnected");
        a.showAndWait();
    }

    /*****
     * oponent reconected info alert
     */
    public static void ocReconnected() {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle("ERROR");
        a.setHeaderText("Your oponent Reconnected to the game");
        a.showAndWait();
    }
}

