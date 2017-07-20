package minesweeper.controllers;

import javafx.application.Platform;
import javafx.fxml.Initializable;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import minesweeper.model.MinesGame;
import minesweeper.model.MinesGameException;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.ResourceBundle;

/**
 * Created by andrey on 19.07.17.
 */
public class GameController implements Initializable {

    public GridPane gridField;
    public Label flagsLabel;
    public Label timeLabel;
    public ImageView flagImage;
    public ImageView clockImage;
    private MinesGame game;
    private int fieldWidth, fieldHeight, minesCount, flagsOnField;
    private int winsCount, losesCount;
    private Stage prevStage;
    private Map<Character, Image> imageMap;

    private Thread timeUpdateThread;
    private Stage currentStage;

    {
        imageMap = new HashMap<>();
        imageMap.put('O', new Image("minesweeper/res/images/opened.png"));
        imageMap.put('F', new Image("minesweeper/res/images/flag.png"));
        imageMap.put('C', new Image("minesweeper/res/images/empty.png"));
        imageMap.put('M', new Image("minesweeper/res/images/mine.png"));
        imageMap.put('R', new Image("minesweeper/res/images/mine-red.png"));
        for (int i = 1; i < 10; i++) {
            imageMap.put(("" + i).charAt(0), new Image("minesweeper/res/images/" + i + ".png"));
        }
    }

    public void backToLevels(MouseEvent mouseEvent) {
        onWindowClose(null);
        currentStage.close();
    }

    public void setCurrentStage(Stage currentStage) {
        this.currentStage = currentStage;
    }


    class FieldNode extends Group {
        private AnchorPane mainPane;

        private final int x, y;
        private char cellAsChar = 'C';

        FieldNode(int x, int y) {
            super();
            this.x = x;
            this.y = y;
            double size = gridField.getPrefWidth() / (fieldWidth > fieldHeight ? fieldWidth : fieldHeight);
            mainPane = new AnchorPane();
            mainPane.setPrefWidth(size);
            mainPane.setPrefHeight(size);
            ImageView image = new ImageView(imageMap.get('C'));
            image.setFitWidth(size);
            image.setFitHeight(size);
            mainPane.getChildren().add(image);
            AnchorPane.setLeftAnchor(image, 0D);
            AnchorPane.setBottomAnchor(image, 0D);
            AnchorPane.setRightAnchor(image, 0D);
            AnchorPane.setTopAnchor(image, 0D);

            super.getChildren().add(mainPane);
        }

        int getX() {
            return x;
        }

        int getY() {
            return y;
        }

        void setChar(char c) {
            cellAsChar = c;
            ImageView imageView = (ImageView) mainPane.getChildren().get(0);
            imageView.setImage(imageMap.get(c));
        }

        char getCellAsChar() {
            return cellAsChar;
        }

    }

    public void replay(MouseEvent mouseEvent) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Хотите с начала?");
        alert.setHeaderText(null);
        alert.setContentText("Играть еще раз?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.get() == ButtonType.OK) {
            prepareGame(fieldWidth, fieldHeight, minesCount);
        }
    }


    public void setPrevStage(Stage prevStage) {
        this.prevStage = prevStage;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        clockImage.setImage(new Image("minesweeper/res/images/clock.png"));
        flagImage.setImage(new Image("minesweeper/res/images/flags.png"));
    }


    public void prepareGame(int fieldWidth, int fieldHeight, int minesCount) {
        prevStage.hide();
        if (timeUpdateThread != null) {
            timeUpdateThread.interrupt();
        }
        try {
            game = new MinesGame(fieldWidth, fieldHeight, minesCount);
        } catch (MinesGameException e) {
            e.printStackTrace();
        }
        gridField.setDisable(false);
        this.fieldWidth = fieldWidth;
        this.fieldHeight = fieldHeight;
        this.minesCount = minesCount;
        for (int i = 0; i < fieldHeight; i++) {
            for (int j = 0; j < fieldWidth; j++) {
                FieldNode node = new FieldNode(j, i);
                node.setOnMouseClicked((event -> {
                    FieldNode clickedNode = (FieldNode) event.getSource();
                    if (event.getButton() == MouseButton.PRIMARY && clickedNode.getCellAsChar() == 'C') {
                        if (!game.openCell(clickedNode.getX(), clickedNode.getY())) {
                            gridField.setDisable(true);
                            losesCount++;
                            updateField();
                            gameOver(false);
                            return;
                        }
                        if (game.isWin()) {
                            winsCount++;
                            gridField.setDisable(true);
                            updateField();
                            gameOver(true);
                            return;
                        }
                        updateField();
                    }
                    if (event.getButton() == MouseButton.SECONDARY) {
                        if (clickedNode.getCellAsChar() == 'F') {
                            if (game.unFlag(clickedNode.getX(), clickedNode.getY()))
                                flagsOnField--;
                        }
                        if (clickedNode.getCellAsChar() == 'C') {
                            if (game.guessMine(clickedNode.getX(), clickedNode.getY()))
                                flagsOnField++;
                        }
                        updateField();
                    }
                }));
                gridField.add(node, j, i);
            }
        }
        flagsOnField = 0;
        updateFlagsLabel();
        timeUpdateThread = new Thread(Thread.currentThread().getThreadGroup(), this::updateTimerLabel);
        timeUpdateThread.start();

    }

    public void onWindowClose(WindowEvent windowEvent) {
        timeUpdateThread.interrupt();
        prevStage.show();
    }

    public Thread getTimeUpdateThread() {
        return timeUpdateThread;
    }

    private void gameOver(boolean win) {
        timeUpdateThread.interrupt();
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Игра окончена");
        alert.setHeaderText("Вы " + (win ? "выиграли" : "проиграли"));
        alert.setContentText("Играть еще раз?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.get() == ButtonType.OK) {
            prepareGame(fieldWidth, fieldHeight, minesCount);
        }
    }

    private void updateField() {
        char[][] charsField = game.getUserField();
        for (Node node :
                gridField.getChildren()) {
            try {
                FieldNode castedNode = ((FieldNode) node);
                castedNode.setChar(charsField[castedNode.getY()][castedNode.getX()]);
            } catch (ClassCastException ignored) {
            }
        }
        updateFlagsLabel();
    }

    private void updateFlagsLabel() {
        flagsLabel.setText(flagsOnField + "/" + minesCount);
    }

    private void updateTimerLabel() {
        long startTime = System.currentTimeMillis();
        while (timeUpdateThread.isAlive()) {
            long currentTime = System.currentTimeMillis() - startTime;
            currentTime /= 1000;
            String minutes = currentTime / 60 < 10 ? "0" + currentTime / 60 : "" + currentTime / 60;
            String seconds = (currentTime - currentTime / 60) < 10 ? "0" + (currentTime - (currentTime / 60)) : "" + (currentTime - currentTime / 60);
            String timeForLabel = minutes + ":" + seconds;
            Platform.runLater(() -> {
                timeLabel.setText(timeForLabel);
            });
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                return;
            }
        }
    }

}