package Parts;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.control.Button;
import javafx.scene.paint.Color;

public class GameBoardButton extends Button {
    private IntegerProperty index_number;
    private ObjectProperty<Color> color;
    private final int size = 40;
    private IntegerProperty index_row;
    private IntegerProperty index_column;


    /****
     * game board button constructor
     * @param index index of button
     */
    public GameBoardButton(int index){
        super("");
        this.index_number = new SimpleIntegerProperty(index);
        setPrefSize(this.size, this.size);
        this.color = new SimpleObjectProperty<Color>(Color.WHITE);
    }

    /***
     * @return present color of button
     */
    public Color getButtonColor(){
        return this.color.get();
    }

    /*****
     * sets color of button
     * @param color new color
     */
    public void setColor(Color color){
        this.color.setValue(color);
    }

}
