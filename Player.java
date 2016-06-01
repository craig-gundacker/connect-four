
import javafx.scene.paint.Color;

/*
Represents a player in the game
*/
public enum Player 
{
    EMPTY(0, "EMPTY", Color.WHITE),
    BLUE(1, "BLUE", Color.BLUE),
    RED(2, "RED", Color.RED);
    
    private final int value;
    private final String name;
    private final Color color;

    Player(int value, String name, Color color)
    {
        this.value = value;
        this.name = name;
        this.color = color;
    }

    public int getValue() {
        return value;
    }

    public String getName() {
        return name;
    }

    public Color getColor() {
        return color;
    }
}
