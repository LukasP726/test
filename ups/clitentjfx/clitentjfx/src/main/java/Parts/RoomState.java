package Parts;

public enum RoomState {
    //TODO - uprav vypisy a UNKNOWN na UNK
    READY("Waiting for start"), PLAYER_DISCONNECTED("Player disconnected"), IN_GAME("In Game"), UNK("?!?!Lost in space and time?!?!");

    public String state;

    /**
     * Constructor
     *
     * @param state state name
     */
    RoomState(String state) {
        this.state = state;
    }

    /**
     * Get state from int (see server .h file for values)
     *
     * @param i int
     * @return state
     */
    public static RoomState getState(int i) {
        switch (i) {
            case 0:
                return READY;
            case 1:
                return PLAYER_DISCONNECTED;
            case 2:
                return IN_GAME;
            default:
                return UNK;
        }
    }

    @Override
    public String toString() {
        return "[" + this.state + "] ";
    }
}
