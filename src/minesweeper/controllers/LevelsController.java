package minesweeper.controllers;

import javafx.beans.binding.Bindings;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Slider;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class LevelsController implements Initializable {


    public GridPane tableWithLevels;
    public BorderPane mainPane;
    public Slider widthSlider;
    public Slider heightSlider;
    public Slider minesSlider;
    private static Stage prevStage;
    public Text informationAboutField;

    public void setPrevStage(Stage prevStage) {
        LevelsController.prevStage = prevStage;
    }


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            minesSlider.maxProperty().bind(
                    Bindings.add(Bindings.multiply(widthSlider.valueProperty(), heightSlider.valueProperty()), -1)
            );
            minesSlider.valueProperty().addListener((obs, oldValue, newValue) -> {
                minesSlider.setValue(Math.round(newValue.doubleValue()));
            });
            widthSlider.valueProperty().addListener((obs, oldValue, newValue) -> {
                widthSlider.setValue(Math.round(newValue.doubleValue()));
            });
            heightSlider.valueProperty().addListener((obs, oldValue, newValue) -> {
                heightSlider.setValue(Math.round(newValue.doubleValue()));
            });
            informationAboutField.textProperty().bind(
                    Bindings.format("Поле %.0f×%.0f, %.0f мин", widthSlider.valueProperty(), heightSlider.valueProperty(), minesSlider.valueProperty())
            );
        } catch (NullPointerException ignored) {
        }
    }

    private void startGame(int fieldWidth, int fieldHeight, int minesCount) throws IOException {
        FXMLLoader myLoader = new FXMLLoader(getClass().getResource("../views/GameView.fxml"));
        Parent root = myLoader.load();
        GameController controller = myLoader.getController();
        Stage gameStage = new Stage();
        gameStage.setTitle("Сапер");
        gameStage.onCloseRequestProperty().setValue(controller::onWindowClose);
        controller.setPrevStage(prevStage);
        controller.setCurrentStage(gameStage);
        controller.prepareGame(fieldWidth, fieldHeight, minesCount);
        Scene scene = new Scene(root);
        gameStage.setScene(scene);
        gameStage.show();
    }

    public void setLevel16(MouseEvent mouseEvent) throws IOException {
        startGame(16, 16, 40);
    }

    public void setLevel30(MouseEvent mouseEvent) throws IOException {
        startGame(30, 16, 99);
    }

    public void setLevel8(MouseEvent mouseEvent) throws IOException {
        startGame(8, 8, 10);
    }

    public void setLevelOther(MouseEvent mouseEvent) throws IOException {
        mainPane.getChildren().set(0, FXMLLoader.load(getClass().getResource("../views/LevelsView-other.fxml")));

    }

    public void startWithUserInput(MouseEvent mouseEvent) throws IOException {
        startGame((int) widthSlider.getValue(), (int) heightSlider.getValue(), (int) minesSlider.getValue());
    }

    public void goBack(MouseEvent mouseEvent) throws IOException {
        mainPane.getChildren().set(0, FXMLLoader.load(getClass().getResource("../views/LevelsView.fxml")));
    }

}