package game;

import java.net.URL;
import java.util.ArrayList;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaException;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.util.Duration;

/*
The classic two player connection game
*/
public class ConnectFour extends Application
{
    private static final int NUM_ROWS = 7;
    private static final int NUM_COLUMNS = 6;
    private static final int NUM_SLOTS = NUM_ROWS * NUM_COLUMNS;
    private static final double BOARD_WIDTH = 600;
    private static final double BOARD_HEIGHT = 650;
    private static final double PADDING = 5;

    private final Player[][] gridPlayers = new Player[NUM_COLUMNS][NUM_ROWS];
    private final Slot[][] gridSlots = new Slot[NUM_COLUMNS][NUM_ROWS];
    private final ArrayList<Slot> alSlot = new ArrayList<>();
    private final ArrayList<Slot> alWinnerSlot = new ArrayList<>();
    
    private Player whoseUp = Player.RED;
    private int numCycles = 0;
    private Label lblMessage;
    private int slotFilledCounter = 0;
    
    private Timeline animSlots;
    private boolean gameWon = false;
    private boolean draw = false;

    private MediaPlayer mPlayerWin;  
    private MediaPlayer mPlayerFill;
    private MediaPlayer mPlayerDrawSound;

    
    @Override
    public void start(Stage primaryStage)
    {
        BorderPane root = new BorderPane();
        root.setTop(initTitle());
        root.setCenter(setUpBoard());
        root.setBottom(activateNewGameButton());
        //root.setRight(activateAudioButton());
        
        Scene scene = new Scene(root, BOARD_WIDTH - 100, BOARD_HEIGHT);
        primaryStage.setTitle("Connect Four");
        primaryStage.setScene(scene);
        primaryStage.show();
    }
    
    public static void entry(String[] args)
    {
        launch(args);
    }
    
    private HBox initTitle()
    {
        HBox hbTitle = new HBox();
        hbTitle.setAlignment(Pos.CENTER);
        lblMessage = new Label();
        lblMessage.setText(whoseUp.getName() + " Player's Turn");
        lblMessage.setTextFill(whoseUp.getColor());
        lblMessage.setFont(Font.font(20));
        hbTitle.getChildren().add(lblMessage);
        return hbTitle;        
    }
    
    private GridPane setUpBoard()
    {
        GridPane board = new GridPane();
        board.setAlignment(Pos.CENTER);
        board.setVgap(PADDING);
        board.setHgap(PADDING);
        double x = BOARD_WIDTH/NUM_COLUMNS;
        double y = BOARD_HEIGHT/NUM_ROWS;
        for (int i = 0; i < NUM_ROWS; i++)
        {
            for (int j = 0; j < NUM_COLUMNS; j++)
            {
                Slot slot = new Slot();
                Circle coin = slot.getCoin();
                coin.setCenterX((x / 2) + (j * x));
                coin.setCenterY((y / 2) + (i * y));
                slot.setX(j);
                slot.setY(i);
                board.add(coin, j, i);
                alSlot.add(slot);
                gridPlayers[j][i] = Player.EMPTY;
                gridSlots[j][i] = slot;
            }
        }
        return board;
    }
    
    private HBox activateNewGameButton()
    {        
        Button btnNewGame = new Button("New Game");
        btnNewGame.setOnAction(e ->
        {
            stopMedia();
            resetBoard();           
        });
        
        HBox hbButton = new HBox(20);
        hbButton.setAlignment(Pos.CENTER);
        hbButton.setPadding(new Insets(5, 5, 5, 5));
        hbButton.getChildren().add(btnNewGame);
        
        return hbButton;
    }
    
    public void stopMedia()
    {
        if (gameWon)
        {
            mPlayerWin.stop();
            animSlots.stop();
            gameWon = false;
        }

        if (draw)
        {
            mPlayerDrawSound.stop();
            draw = false;
        }        
    }
    
    public void resetBoard()
    {
        for (Slot slot: alSlot)
        {
            slot.resetFill();
            slot.setFilled(false);
        }

        for (int i = 0; i < NUM_ROWS; i++)
        {
            for (int j = 0; j < NUM_COLUMNS; j++)
            {
                gridPlayers[j][i] = Player.EMPTY;
            }
        }        
    }
    
    private VBox activateAudioButton()
    {
        VBox vbVolume = new VBox(20);
        vbVolume.setAlignment(Pos.BOTTOM_CENTER);
        vbVolume.setPadding(new Insets(0, 5, 0, 0));
        
        try
        {
            Image imgSoundOn = new Image(getClass().getResourceAsStream("resources/sound.png"));
            ImageView imgViewSoundOn = new ImageView(imgSoundOn);
            Image imgSoundOff = new Image(getClass().getResourceAsStream("resources/sound_delete.png"));
            ImageView imgViewSoundOff = new ImageView(imgSoundOff);
            
            Button btnAudio = new Button();
            btnAudio.setGraphic(imgViewSoundOn);
            btnAudio.setOnAction(e ->
            {
                if (btnAudio.getGraphic().equals(imgViewSoundOn))
                {
                    btnAudio.setGraphic(imgViewSoundOff);
                    mPlayerWin.setMute(true);
                    mPlayerFill.setMute(true);
                }
                else
                {
                    btnAudio.setGraphic(imgViewSoundOn);
                    mPlayerWin.setMute(false);
                    mPlayerFill.setMute(false);
                }            
            });

            Slider slVolume = new Slider(0, 100, 50);
            slVolume.setOrientation(Orientation.VERTICAL);
            loadMediaPlayers(slVolume);

            vbVolume.getChildren().addAll(slVolume, btnAudio);
        }
        catch (NullPointerException ex)
        {
            ex.printStackTrace();
        }
        return vbVolume;
    }

    private void loadMediaPlayers(Slider slVolume)
    {
        try
        {
            
            URL resourceWinSound = getClass().getResource("resources/fireworks.mp3");
            Media mediaWin = new Media(resourceWinSound.toExternalForm());
            mPlayerWin = new MediaPlayer(mediaWin);
            mPlayerWin.volumeProperty().bind(slVolume.valueProperty().divide(100));

            URL resourceFillSound = getClass().getResource("resources/tap.mp3");
            Media mediaFill = new Media(resourceFillSound.toExternalForm());        
            mPlayerFill = new MediaPlayer(mediaFill);
            mPlayerFill.volumeProperty().bind(slVolume.valueProperty().divide(100));
        }
        catch(SecurityException | IllegalArgumentException |  UnsupportedOperationException | MediaException ex)
        {
            System.err.println(ex.getMessage());
        }
    }
    
    public class Slot extends Pane
    {
        boolean filled;
        Circle coin;
        int slotX;
        int slotY;
        
        public Slot()
        {
            filled = false;
            coin = new Circle();
            coin.setRadius((BOARD_WIDTH / NUM_COLUMNS) / 2.5 - PADDING);
            coin.setFill(Player.EMPTY.getColor());
            coin.setStroke(Color.BLACK);
            coin.setOnMouseClicked(new CoinClickHandler());
            getChildren().add(coin);
        }
        
        public void setFilled(boolean state)
        {
            this.filled = state;
        }
        
        public boolean isFilled()
        {
            return filled;
        }
        
        public Circle getCoin()
        {
            return coin;
        }
        
        public void setFill(Player player)
        {
            coin.setFill(player.getColor());
        }

        public void resetFill()
        {
            coin.setFill(Player.EMPTY.getColor());
        }
        
        public void setX(int posX)
        {
            slotX = posX;
        }
        
        public void setY(int posY)
        {
            slotY = posY;
        }
        
        public int getX()
        {
            return slotX;
        }
        
        public int getY()
        {
            return slotY;
        }
        
        private class CoinClickHandler implements EventHandler<MouseEvent>
        {
            @Override
            public void handle(MouseEvent e)
            {
                if (validSlot())
                {
                    lockBoard();
                    Timeline animation = coinDropAnim(whoseUp);
                    /*
                    Coin has reached terminal position
                    */
                    animation.setOnFinished(ev ->
                    {
                        slotFilledCounter++;
                        coin.setFill(whoseUp.getColor());
                        unLockBoard();
                        gridPlayers[slotX][slotY] = whoseUp;
                        gameWon = checkWinner(whoseUp);
                        if (gameWon)
                        {
                            broadcastWinner();
                        }
                        else
                        {
                            draw = checkDraw();
                            if (draw)
                            {
                                broadcastDraw();
                            }
                            else
                            {
                                whoseUp = switchPlayer();
                                lblMessage.setTextFill(whoseUp.getColor());
                                lblMessage.setText(whoseUp + " Player's Turn");                                
                            }
                        }
                        mPlayerFill.stop();
                        mPlayerFill.play();                        
                    });
                }
            }
        }
        
        private boolean validSlot()
        {
            return (slotY == NUM_ROWS - 1 || !Player.EMPTY.equals(gridPlayers[slotX][slotY + 1])) 
                    && !isFilled();
        }
            
        private Timeline coinDropAnim(Player player)
        {
            ArrayList<ConnectFour.Slot> emptySlots = new ArrayList<>();
            for (int i = 0; i <= slotY; i++)
            {
                emptySlots.add(gridSlots[slotX][i]);
            }

            Color fillColor = player.getColor();

            Timeline dropCoinAnim = new Timeline(new KeyFrame(Duration.millis(150), e ->
            {
                if (numCycles > 0)
                {
                    ConnectFour.Slot flashOff = emptySlots.get(numCycles - 1);
                    Circle turnOff = flashOff.getCoin();
                    turnOff.setFill(Player.EMPTY.getColor());
                }

                ConnectFour.Slot flashOn = emptySlots.get(numCycles);
                Circle c = flashOn.getCoin();
                c.setFill(fillColor);

                numCycles++;                
            }));
            dropCoinAnim.setCycleCount(emptySlots.size());
            dropCoinAnim.play();
            numCycles = 0;

            return dropCoinAnim;
        }
        
        private boolean checkDraw()
        {
            return slotFilledCounter == NUM_SLOTS;
        }
        
        private void broadcastDraw()
        {
            try
            {
            lblMessage.setTextFill(Color.GREEN);
            lblMessage.setText("It's a draw");
            URL resourceDrawSound = this.getClass().getResource("draw.mp3");
            Media mediaDrawSound = new Media(resourceDrawSound.toExternalForm());
            mPlayerDrawSound = new MediaPlayer(mediaDrawSound);
            mPlayerDrawSound.play();
            }
            catch (SecurityException | IllegalArgumentException |  UnsupportedOperationException | MediaException ex )
            {
                System.err.println(ex.getMessage());
            }
        }
        
        private boolean checkWinner(Player player)
        {
            Color playerColor = player.getColor();
            if (rowConnect(playerColor))
                return true;
            else if (colConnect(playerColor))
                return true;
            else if (posDiagonalConnect(playerColor))
                return true;
            else if (negDiagonalConnect(playerColor))
                return true;
            else
                return false;
        }
    
        private boolean rowConnect(Color playerColor)
        {
            int count;
            for (int i = 0; i < NUM_ROWS; i++)
            {
                count = 0;
                alWinnerSlot.clear();
                for (int j = 0; j < NUM_COLUMNS; j++)
                {
                    if (gridPlayers[j][i].getColor().equals(playerColor))
                    {
                        count++;
                        ConnectFour.Slot slot = gridSlots[j][i];
                        alWinnerSlot.add(slot);
                        if (count == 4)
                        {
                            return true;
                        }
                    }
                    else
                    {
                        count = 0;
                        alWinnerSlot.clear();
                    }
                }
            }
            return false;
        }

        private boolean colConnect(Color playerColor)
        {
            int count;
            for (int i = 0; i < NUM_COLUMNS; i++)
            {
                count = 0;
                alWinnerSlot.clear();
                for (int j = 0; j < NUM_ROWS; j++)
                {
                    if (gridPlayers[i][j].getColor().equals(playerColor))
                    {
                        count++;
                        ConnectFour.Slot slot = gridSlots[i][j];
                        alWinnerSlot.add(slot);
                        if (count == 4)
                        {
                            return true;
                        }
                    }
                    else
                    {
                        count = 0;
                        alWinnerSlot.clear();
                    }
                }
            }
            return false;
        }

        private boolean posDiagonalConnect(Color playerColor)
        {
            int count = 0;
            for (int h = 0; h < 3; h++)
            {
                int i = h + 3;
                count = 0;
                alWinnerSlot.clear();
                for (int j = 0; j < h + 4; i--, j++)
                {
                    if (playerColor.equals(gridPlayers[i][j].getColor()))
                    {
                        count++;
                        ConnectFour.Slot slot = gridSlots[i][j];
                        alWinnerSlot.add(slot);
                        if (count == 4)
                        {
                            return true;
                        }
                    }
                    else 
                    {
                        count = 0;
                        alWinnerSlot.clear();
                    }
                }
            }

            for (int h = 0; h < 3; h++)
            {
                int i = 5;
                count = 0;
                alWinnerSlot.clear();
                for (int j = h + 1; j < NUM_ROWS; i--, j++)
                {
                    if (playerColor.equals(gridPlayers[i][j].getColor()))
                    {
                        count++;
                        ConnectFour.Slot slot = gridSlots[i][j];
                        alWinnerSlot.add(slot);
                        if (count == 4)
                        {
                            return true;
                        }
                    }
                    else 
                    {
                        count = 0;
                        alWinnerSlot.clear();
                    }
                }
            }
            return false;
        }

        private boolean negDiagonalConnect(Color playerColor)
        {
            //Check negative diagonals
            int count;
            for (int h = 0; h < 3; h++)
            {
                count = 0;
                alWinnerSlot.clear();
                for (int i = 0, j = h + 1; j < NUM_ROWS; i++, j++)
                {
                    if (playerColor.equals(gridPlayers[i][j].getColor()))
                    {
                        count++;
                        ConnectFour.Slot slot = gridSlots[i][j];
                        alWinnerSlot.add(slot);
                        if (count == 4)
                        {
                            return true;
                        }        
                    }
                    else
                    {
                        count = 0;
                        alWinnerSlot.clear();
                    } 
                }
            }

            for (int h = 0; h < 3; h++)
            {
                count = 0;
                alWinnerSlot.clear();
                for (int i = h, j = 0; j < NUM_COLUMNS - h; i++, j++)
                {
                    if (playerColor.equals(gridPlayers[i][j].getColor()))
                    {
                        count++;
                        ConnectFour.Slot slot = gridSlots[i][j];
                        alWinnerSlot.add(slot);
                        if (count == 4)
                        {
                            return true;
                        }        
                    }
                    else
                    {
                        count = 0;
                        alWinnerSlot.clear();
                    } 
                }
            }
            return false;
        }
        
        private Player switchPlayer()
        {
            if (whoseUp.equals(Player.RED))            
                whoseUp = Player.BLUE;           
            else            
                whoseUp = Player.RED;            
            return whoseUp;
        }
        
        private void broadcastWinner()
        {
            lockBoard();
            mPlayerWin.play();
            
            lblMessage.setTextFill(whoseUp.getColor());
            lblMessage.setText(whoseUp + " Player Wins");
            
            ArrayList<Color> colorArray = new ArrayList<>();
            colorArray.add(Color.PURPLE);
            colorArray.add(Color.ORANGE);
            colorArray.add(Color.GREEN);
            colorArray.add(Color.CYAN);

            animSlots = new Timeline(new KeyFrame(Duration.millis(300), e ->
            {
                for (int i = 0; i < alWinnerSlot.size(); i++)
                {
                    ConnectFour.Slot s = alWinnerSlot.get(i);
                    Circle c = s.getCoin();
                    c.setFill(colorArray.get(i));
                }
                int size = colorArray.size();
                Color c = colorArray.get(size - 1);
                colorArray.remove(size - 1);
                colorArray.add(0, c);                
            }));
            animSlots.setCycleCount(Timeline.INDEFINITE);
            animSlots.play();
        }
        
        private void lockBoard()
        {
            for (Slot slot: alSlot)
            {
                slot.setFilled(true);
            }
        }
        
        private void unLockBoard()
        {
            for (Slot slot: alSlot)
            {
                if (slot.getCoin().getFill().equals(Color.WHITE))
                {
                    slot.setFilled(false);
                }
            }
        }
 
    }            
}
