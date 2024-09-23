package Parts;

public enum PlayerState {
    CONNECTED("[Connected] "), DISCONNECTED("[Disconnected] "), UNK("[LOST IN SPACE AND TIME] ");

    private String state;

    /**
     * constructor
     *
     * @param state state name
     */
    PlayerState(String state) {
        this.state = state;
    }

    /**
     * Get state from int (see server .h file for values)
     *
     * @param i int
     * @return state
     */
    public static PlayerState getState(int i) {
        switch (i) {
            case 10:
                return CONNECTED;
            case 11:
                return DISCONNECTED;
            default:
                return UNK;
        }
    }

    @Override
    public String toString() {
        return this.state;
    }
}
