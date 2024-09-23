package Parts;

public class PlayerInfo {
    private String username;
    private PlayerState playerState;
    private boolean first_player;


    public PlayerInfo(String username, PlayerState playerState, boolean first_player) {
        this.username = username;
        this.playerState = playerState;
        this.first_player = first_player;
    }

    /**
     * Constructor
     *
     * @param username username
     * @param playerState player state
     */
    public PlayerInfo(String username, PlayerState playerState) {
        this.username = username;
        this.playerState = playerState;
    }

    /**********
     * getter for username of player
     * @return players name
     */
    public String getUsername() {
        return this.username;
    }


    @Override
    public String toString() {
        return String.format(this.username + "  <" + this.playerState + ">");
    }
}
