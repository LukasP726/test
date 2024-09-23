package Parts;


public class RoomInfo {
    private int ID;
    private RoomState state;
    private int players_in;
    private PlayerInfo [] playerInfo;

    /************
     *
     * @param roomID room ID
     * @param roomState stav
     * @param numOfPlayers poc hracu
     * @param players pole hracu
     */
    public RoomInfo(int roomID, RoomState roomState, int numOfPlayers, PlayerInfo[] players){
        this.ID = roomID;
        this.state = roomState;
        this.players_in = numOfPlayers;
        this.playerInfo = players;
    }

    public int getID() {
        return ID;
    }

    /****
     * returns info about all players
     * @return playersInfo
     */
    public String getPlayersInfo(){
        String out = this.playerInfo[0].toString();
        if (this.playerInfo.length == 2){
            out += this.playerInfo[1].toString();
        }
        return out;
    }

    /*****
     * returns info about one player
     * @param i player in array
     * @return player
     */
    public PlayerInfo getplayer(int i){
        if(i < 2)
            return this.playerInfo[i];
        else
            return this.playerInfo[1];
    }

    /****
     * getter for all palyerInfo
     * @return both players info
     */
    public PlayerInfo[] getPlayerInfo() {
        return playerInfo;
    }

    /****
     * adds second player to the room
     * @param player second player
     */
    public void addSecondPlayer(PlayerInfo player){
        if (this.playerInfo.length == 1){
            PlayerInfo[] playerInfos = new PlayerInfo[this.playerInfo.length + 1];
            playerInfos[0] = this.playerInfo[0];
            playerInfos[1] = player;
            this.playerInfo = playerInfos;
        }
    }

    @Override
    public String toString() {
        return this.playerInfo[0].getUsername() + "'s room <" + this.state + ">";
    }
}
