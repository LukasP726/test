package Net;

import Controllers.BoardControl;
import Controllers.StageController;
import Parts.*;
import javafx.application.Platform;
import main.Main;

import javax.swing.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.*;
import java.util.ArrayList;

public class Net{
    private String address = "127.0.0.1";
    private int port = 1234;
    private Socket socket;
    private boolean socket_ready;
    private PrintWriter writer;
    private Reader reader;
    public boolean not_in_game = false;
    public StageController controller;
    private RoomInfo playRoom = null;
    private BoardControl boardController;
    private boolean firstPlayer;
    private String thisPlayersName;
    private String roomID;
    private Timer pingRefreshTimer;
    private Thread mess;
    private int c;


    /*****
     * net constructor for reconnect, reuses previous info
     * @param address server address
     * @param controller stagecontroller
     * @param not_in_game player in game/not
     * @param playRoom room to reconnect to
     * @param boardController controller for board actions
     * @param firstPlayer who is first player
     * @param thisPlayersName name of this client
     * @param roomID id of room
     * @param inGame whether player is in game or not
     * @param socket communicating socket
     */
    public Net(String address, StageController controller, boolean not_in_game, RoomInfo playRoom, BoardControl boardController, boolean firstPlayer, String thisPlayersName, String roomID, boolean inGame, Socket socket){
        String[] ipPlusPort = address.split(":");
        this.address = ipPlusPort[0];
        this.c = Main.c++;
        this.port = Integer.parseInt(ipPlusPort[1]);
        this.controller = controller;
        this.socket = socket;

        this.not_in_game = !inGame;
        this.playRoom = playRoom;
        this.boardController = boardController;
        this.firstPlayer = firstPlayer;
        this.thisPlayersName = thisPlayersName;
        this.roomID = "" + playRoom.getID();
        //System.out.println("jdu do recommunicate");
        System.out.println("playerName: " + thisPlayersName + "; room_id: " + this.roomID);
        if (!recommunicate(inGame)) {
                Platform.runLater(() -> Alerts.focedExit("Connection could not be reestablished", -5, this.controller.log));
            }
        System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~IN - GAME? : " + inGame);
        if (inGame){
            System.out.println("Writting reconnect...dotaz");
            writeMessage("RECONNECT|" + this.thisPlayersName + "|" + this.roomID +"\n");
        }
    }

    /*****
     * restarts communication with server via new socket
     * @param in_game client in game/not
     * @return true - connected fine, false - well...try again?
     */
    private boolean recommunicate(boolean in_game){

        try {
            //System.out.println("v recommunicate");
            System.out.println("in game: " + in_game);
            this.writer = new PrintWriter(socket.getOutputStream(), true);
            this.reader = new Reader(this.socket, this);
            this.reader.inGame = in_game;
            this.socket_ready = true;
        }
        catch (IOException e){
            //e.printStackTrace();
            return false;
        }

        pingRefreshTimer = new Timer(500, arg0 -> {
            if (this.socket_ready) {
                if (this.not_in_game) {
                    writeMessage("REFRESH\n");
                }
                else {
                    writeMessage("PING\n");
                }
            }
        });
        System.out.println("start pinging");
        pingRefreshTimer.start();
        System.out.println("start reader");
        Thread readerThread = new Thread(this.reader);
        readerThread.start();
        System.out.println("Returning");
        return true;
    }

    /****
     * constructor for new name comm usage
     * @param address address of server
     * @param controller stagecontroller
     */
    public Net(String address, StageController controller){
        String[] ipPlusPort = address.split(":");
        this.address = ipPlusPort[0];
        this.c = Main.c++;
        this.port = Integer.parseInt(ipPlusPort[1]);
        this.controller = controller;
        this.mess = new Thread(() -> {
            if (!communicate()) {
                Platform.runLater(() -> Alerts.commonError("Connection could not be established!", controller.log));
            }
        });
        mess.start();
    }

    /*****
     * starts communication
     * @return true - works, false - whelp...
     */
    private boolean communicate() {
        try {
            readyASocket();
            System.out.println("redied socket");
            Platform.runLater(() -> {this.controller.createLobbyScene();
                                     controller.showLobbyScene();});
        }
        catch (Exception e){
            //e.printStackTrace();
            Platform.runLater(() -> Alerts.commonError("Could not connect to server", controller.log));
            return false;
        }

        pingRefreshTimer = new Timer(500, arg0 -> {
            if (this.socket_ready) {
                if (this.not_in_game) {
                    writeMessage("REFRESH\n");
                }
                else {
                    writeMessage("PING\n");
                }
            }
        });
        pingRefreshTimer.start();
        this.reader.run();
        return true;
    }


    /**
     * Guess what
     *
     * @throws IOException IOException
     */
    private void readyASocket() throws IOException {
        this.socket = new Socket(this.address, this.port);
        this.writer = new PrintWriter(socket.getOutputStream(), true);
        this.reader = new Reader(this.socket, this);
        this.socket_ready = true;
        System.out.println("client connected");
    }

    /*****
     * reconnects a socket to server
     * @param times times I am willing to try to do so
     * @param inGame was in game or not?
     */
    public void reconnectingSocket(int times, boolean inGame){
        //System.out.println("bool in_Game: " + Boolean.toString(inGame));
        int test = 1;
        close();
        while(test <= times){
            Socket socketino = new Socket();
            try {
                System.out.println("Connecting to: " + this.address + ":" + this.port);
                socketino.connect(new InetSocketAddress(this.address, this.port));
                System.out.println("playerName: " + controller.getName() + "; room_id: " + this.playRoom.getID());
                this.controller.redoNet(this.address + ":" + this.port, this.controller, this.not_in_game, this.playRoom, this.boardController, this.firstPlayer, this.controller.getName(), this.roomID, inGame, socketino);
                return;
            }
            catch (IOException e) {
                System.out.println("Didnt work.........." + test + "/" + times);
                //e.printStackTrace();
                try {
                    Thread.sleep(60000/times, 0);
                }
                catch (Exception f){
                    System.out.println("sleep off");
                    this.controller.log.close();
                    System.exit(-100);
                }
                test++;
            }

        }
        Platform.runLater(() -> Alerts.focedExit("Cannot reconnect", -6, this.controller.log));
    }

    /****
     * writes to server
     * @param message written message
     */
    public synchronized void writeMessage(String message){
        String []test = message.split("\\|");
        String tested = test[0].trim();
        if (!((tested.equals("PING")) || (tested.equals("OK")) || (tested.equals("REFRESH")) ||
                (tested.equals("RECONNECT")) || (tested.equals("TURN")) || (tested.equals("CREATE")) ||
                (tested.equals("JOIN")) || (tested.equals("ROOMS")) || (tested.equals("GAME")))){
            System.out.println("ERROR - IMPOSSIBLE MESSAGE!!!!");
            this.controller.log.close();
            System.exit(-1);
        }
        //System.out.println("message_out: " + message);
        this.controller.log.toLog("Mess_OUT: " + message);
        try {
            this.writer.write(message);
            this.writer.flush();
            if (!message.equals("OK|PING\n") && !message.equals("PING\n"))
                System.out.println("MESS_OUT: " + message);
        } catch (Exception e) {
            System.out.println("WRITER Error");
            //e.printStackTrace();
        }
    }


    /**********
     * ukonci propojeni se serverem
     */
    //TODO dodej do alertů
    public void close() {
        try {
            this.reader.stop();
            this.writer.close();
            this.socket.close();
            this.pingRefreshTimer.stop();
        }
        catch (Exception e) {
            this.controller.log.close();
            System.exit(-10);
        }
    }


    /*****
     * joining room
     * @param joined joining room newly?
     * @param roomID room id
     * @param host_username username of host to whom Im connecting
     * @param client_username my username
     */
    public void joined(boolean joined, int roomID, String host_username, String client_username) {
        if (joined) {
            if (this.playRoom == null){
                PlayerInfo me = new PlayerInfo(controller.getName(), PlayerState.CONNECTED, false);
                PlayerInfo oponent = new PlayerInfo(host_username, PlayerState.CONNECTED, true);
                this.playRoom = new RoomInfo(roomID, RoomState.getState(1), 2, new PlayerInfo[]{oponent, me});
                this.firstPlayer = false;
            }
            else{
                PlayerInfo oponent = new PlayerInfo(client_username, PlayerState.CONNECTED, true);
                this.playRoom.addSecondPlayer(oponent);
                this.firstPlayer = true;
            }
        }
        else{
            //Alerts.joinError();
            System.out.println("CANNOT JOIN!!!!!");
        }
    }

    /**
     * ze stringu nacte stav hry a posle do vytvorene hry
     * */
    public void loadGame(String []s) {
        Platform.runLater(() -> this.controller.loadGame(s, this.playRoom));
    }

    /****************
     * po vytvoreni mistnosti klientem
     */
    public void room_created(String roomID) {
        PlayerInfo me = new PlayerInfo(this.controller.getName(), PlayerState.CONNECTED);
        this.thisPlayersName = this.controller.getName();
        this.roomID = roomID;
        PlayerInfo[] pl = new PlayerInfo[]{me};
        this.playRoom = new RoomInfo(Integer.parseInt(roomID), RoomState.getState(0), 1, pl);
        this.firstPlayer = true;
        this.not_in_game = !this.not_in_game;
        this.controller.room_created();
    }

    /***
     * updates room list
     */
    public void updateRoomList(){
        writeMessage("REFRESH\n");
    }

    /****
     * changes whether in game or not
     */
    public void changeNotInGame(){this.not_in_game = !this.not_in_game;}


    /*****
     * refreshes room list
     * @param rooms roomlist
     */
    public void refresh_rooms(ArrayList<RoomInfo> rooms) {
        if (rooms != null){
            System.out.println("arrayList: ");

            for (int i = 0; i < rooms.size(); i++) {
                System.out.println(rooms.get(i).toString());
            }
        }

        this.controller.refreshRoomList(rooms);
    }

    /***
     * sends move to game gui
     * @param x x- axis
     * @param y y- axis
     * @param player player playing
     */
    public void sendMoveToGame(int x, int y, boolean player) {
        System.out.println("sending move to game");
        //String name = player? this.playRoom.getPlayerInfo()[0].getUsername() : this.playRoom.getPlayerInfo()[1].getUsername();
        Platform.runLater(() -> this.controller.doMove(x, y));

    }

    /***
     * starts a game
     */
    public void startGame() {//if neexistuje 'this.playRoom', kde hraju, jinak to ignoruj!!
        if (this.playRoom == null){
            System.out.println("playRoom == null!!!!!");
            return;
        }
        Platform.runLater(() -> this.boardController = this.controller.startGame(this.playRoom));
    }

    /****
     * ends a game
     * @param winner who won?
     */
    public void endGame(boolean winner) {
        System.out.println("----------------------------------");
        System.out.println("Winner: " + winner);
        System.out.println("=> winner: " + (winner? this.playRoom.getPlayerInfo()[0].getUsername() : this.playRoom.getPlayerInfo()[1].getUsername()));
        System.out.println("pl1: " + this.playRoom.getPlayerInfo()[0].getUsername() + ";; pl2: " + this.playRoom.getPlayerInfo()[1].getUsername());
        Platform.runLater(() -> {
            System.out.println(winner);
            System.out.println(playRoom.getPlayerInfo()[1].getUsername());
            System.out.println(playRoom.getPlayerInfo()[0].getUsername());
            controller.boardControl.commenceVictory(winner? playRoom.getPlayerInfo()[1].getUsername() : playRoom.getPlayerInfo()[0].getUsername());
            playRoom = null;
        });
    }

    /****
     * in whitch room are we playing?
     * @return AAA! in this one!
     */
    public RoomInfo getPlayRoom() {
        return playRoom;
    }

    /***
     * well? is it?
     * @return yes! / no...sry
     */
    public boolean isSocket_ready() {
        return socket_ready;
    }


    /****
     * forces way out of game to lobby
     */
    public void forceLobby() {

        Platform.runLater(() -> this.controller.backToLobby());
    }

    /****
     * reconecting through joining the game
     * @param splitted message written about it
     */
    public void join_reconnect(String[] splitted) {
        PlayerInfo one = new PlayerInfo(splitted[3], PlayerState.CONNECTED, false);
        PlayerInfo two = new PlayerInfo(splitted[4], PlayerState.CONNECTED, true);
        this.playRoom = new RoomInfo(Integer.parseInt(splitted[2]), RoomState.getState(1), 2, new PlayerInfo[]{one, two});
        String[] split_out  = new String[splitted.length - 3];
        int j = 0;
        split_out[j] = splitted[j];
        j++;
        split_out[j] = splitted[j];
        j++;
        for (int i = 5; i < splitted.length; i++) {
            split_out[j] = splitted[i];
            j++;
        }
        Platform.runLater(() -> this.controller.loadGame(split_out, this.playRoom));
    }
}