package Net;

import Parts.*;
import javafx.application.Platform;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.*;
import java.util.ArrayList;



public class Reader implements Runnable{
    private Socket socket;
    public BufferedReader reader;
    private Net net;
    public boolean inGame = false;
    private boolean socket_on = true;


    /*****
     * constructor of reading thread
     * @param socket socket to read from
     * @param net commanding class
     */
    public Reader(Socket socket, Net net){
        this.socket = socket;
        this.net = net;
        try {
            this.reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        }
        catch (Exception e){
            System.out.println("READER ERROR");
        }
    }

    /*****
     * reads message and appropriately reacts
     */
    @Override
    public void run() {
        String message = null;
        while(socket_on){
            message = null;
            String [] splitted;
            try {
                message = this.reader.readLine();
                //System.out.println("---------------------------------------------------message: " + message);
                if (message != null){
                    //System.out.println("Mess_in:  " + message + "\n");
                    this.net.controller.log.toLog("Message_IN: " + message);
                    //System.out.println(message);
                    if (message.equals("PING")){
                        net.writeMessage("OK|PING\n");
                        continue;
                    }
                    splitted = message.split("\\|");
                    //System.out.println(splitted[0]);
                    //TODO if-elsy podle zpráv
                    String tested = splitted[0].trim();
                    if (!((tested.equals("PING")) || (tested.equals("OK")) || (tested.equals("REFRESH")) ||
                            (tested.equals("RECONNECT")) || (tested.equals("TURN")) || (tested.equals("CREATE")) ||
                            (tested.equals("JOIN")) || (tested.equals("ROOMS")) || (tested.equals("GAME")) ||
                            (tested.equals("OPONENTOFF")) || (tested.equals("PLAYER_DC")) || (tested.equals("ERROR")) || (tested.equals("PLAYER_RECONNECTED")) )){
                        System.out.println("WRONG SERVER! DISCONNECTING!");
                        System.out.println("MESSAGE[0]: " + tested);

                        System.exit(-1);
                    }

                    if(splitted[0].equals("ERROR")){
                        if( splitted.length < 2){
                            this.net.controller.log.toLog("ERR - WRONG_MESSAGE: " + message + "\n but continue...");
                        }
                        if (splitted[1].equals("JOIN")){
                            this.net.joined(false,  -1, splitted[2], splitted[3]);
                        }
                        if (splitted[1].equals("RECONNECT")){
                            System.out.println("forcing to lobby");
                            Platform.runLater(() -> {
                                Alerts.commonError(splitted[2], this.net.controller.log);
                            });
                            this.net.forceLobby();
                            continue;
                        }
                        else if (splitted[1].equals("TURN")){
                            //TODO zkontroluj si to, až budeš víc vzhůru!!!!!
                            //net.sendMove(Integer.parseInt(splitted[2]), Integer.parseInt(splitted[3]), Boolean.parseBoolean(splitted[4]));
                            String[] finalSplitted = splitted;
                            Platform.runLater(() -> Alerts.invalidTurnAlert(finalSplitted[2]));
                            continue;
                        }
                    }
                    else if(splitted[0].equals("GAME")){
                        if (splitted[1].equals("START")){
                            this.inGame = true;
                            this.net.not_in_game = false;
                            this.net.startGame();
                        }
                        else if(splitted[1].equals("END")){
                            this.net.endGame(!(splitted[2].equals("0")));
                            continue;
                        }
                    }
                    else if(splitted[0].equals("ROOMS")){ //ROOMS|0|0|plr|   splitted[0].length() > 4 && splitted[0].substring(0, 5).equals("ROOMS")
                        //System.out.println("IN ROOMS");
                        ArrayList<RoomInfo>rooms = new ArrayList<>();
                        String [] player;
                        for (int i = 1; i < splitted.length - 1; i += 4) {
                            System.out.println("ROOM_" + i);
                            System.out.println(splitted[i]);
                            System.out.println(splitted[i + 1]);
                            System.out.println(splitted[i + 2]);
                            System.out.println(splitted[i + 3]);
                            System.out.println();
                            int roomID = Integer.parseInt(splitted[i]);
                            RoomState roomState = RoomState.getState(Integer.parseInt(splitted[i + 1]));
                            int numOfPlayers = Integer.parseInt(splitted[i + 2]);
                            player = splitted[i + 3].split("\\$");
                            PlayerInfo[] players = new PlayerInfo[numOfPlayers];
                            players[0] = new PlayerInfo(player[0], PlayerState.getState(Integer.parseInt(player[1])));
                            if (numOfPlayers > 1){
                                System.out.println("players > 1");
                                System.out.println(splitted[i + 4]);
                                player = splitted[i + 4].split("\\$");
                                players[1] = new PlayerInfo(player[0], PlayerState.getState(Integer.parseInt(player[1])));
                                i++;
                            }
                            RoomInfo roomie = new RoomInfo(roomID, roomState, numOfPlayers, players);
                            rooms.add(roomie);
                        }
                        net.refresh_rooms(rooms);
                        continue;
                    }
                    else if(splitted[0].equals("OK") && splitted.length > 1){
                        if( splitted.length < 2){
                            this.net.controller.log.toLog("ERR - WRONG_MESSAGE: " + message + "\n but continue...");
                        }
                        if (splitted[1].equals("CREATE")){
                            // this.net.writeInMyRoom(Integer.parseInt(splitted[2]), RoomState.getState(Integer.parseInt(splitted[3])));
                            this.net.room_created(splitted[2]);
                            continue;
                        }
                        else if(splitted[1].equals("JOIN")){
                            this.net.joined(true, Integer.parseInt(splitted[2]), splitted[3], splitted[4]);
                            continue;
                        }
                        else if(splitted[1].equals("RECONNECT")){
                            this.net.loadGame(splitted);
                            continue;
                        }

                        else if(splitted[1].equals("JOIN_RECONNECT")){
                            this.net.join_reconnect(splitted);
                            continue;
                        }

                        else if (splitted[1].equals("TURN")){
                            net.sendMoveToGame(Integer.parseInt(splitted[3]), Integer.parseInt(splitted[4]), Boolean.parseBoolean(splitted[2]));
                            continue;
                        }

                    }
                    else if(splitted[0].equals("OPONENTOFF\n") || splitted[0].equals("OPONENTOFF")){
                        //TODO odpoj se ze hry
                        System.out.println("Oponent je off, měl bych se odpojit");
                        Platform.runLater(Alerts::oponentOff);
                        this.net.forceLobby();
                        continue;
                    }

                    else if(splitted[0].equals("PLAYER_DC")){
                        Platform.runLater(Alerts::opDisconnected);
                    }

                    else if(splitted[0].equals("PLAYER_RECONNECTED")){
                        Platform.runLater(Alerts::ocReconnected);
                    }
                }
                else {
                    System.out.println("NULL_MESSAGE");
                    try {
                        reader.close();
                    } catch (Exception e) {
                        System.out.println("READER DIDNT STOP");
                    }
                }


            }
            catch (IOException e) {
                Platform.runLater(Alerts::disconnected);
                this.net.reconnectingSocket(20, this.inGame);
                break;
            }

        }

    }

    public void stop() {
        try {
            reader.close();
        } catch (IOException e) {
            System.out.println("READER DIDNT STOP");
        }
    }



}
