package Parts;

public class InGameTexts {
    public static String getSettingsHelpText(){
        //todo - až upravíš additional setting, tady to taky uprav...
        return "To start a game, you need to choose a different name for both players and different color for each players tiles. In additional settings you can set number of buttons in one row/column for each player.";
    }
    public static String getGameHelpText(){
        return "To win the game player have to connect tiles on opposing sides of the board starting and ending with their color. when two neighboring tiles are changed to their color, a tile in between also gets under their control and connects. Already colored tiles cannot be abducted by opposing player.";
    }
    public static String getAboutText(){
        return "Version: 1.0\nAuthor: Daniel Caba, A18B0183P\nZCU Plzen, 2021";
    }
    public static String resetAsk(){return "Are you sure you want to reset the game?";}
}
