package Controllers;

import Graphics.BoardView;
import Net.Net;
import Parts.RoomInfo;
import javafx.application.Platform;
import javafx.scene.paint.Color;
import java.util.ArrayList;

public class LobbyController {
    public StageController stageController;
    private boolean visibleLabel = false;

    /**
     * constructor
     * @param stageController stagecontroller
     */
    public LobbyController(StageController stageController){
        this.stageController = stageController;

    }


    public void updateRoomList(){
        this.stageController.updateRoomList();
    }

    public double getMinWeight() {
        return this.stageController.getSettingMinWight();
    }

    public double getMinHeight() {
        return this.stageController.getSettingMinHeight();
    }

    public void createRoom(String name) {
        this.stageController.createRoom(name);
    }

    public void sendGameStartQuestion(RoomInfo room, String name) {
        this.stageController.sendGameStartQuestion(room, name);
    }

    //TODO smaž, když nepoužiješ!
    /*public void rewind(String name, ArrayList<RoomInfo> rooms){
        stageController.rewindSettingsScene(this, name, rooms);
    }*/

}

