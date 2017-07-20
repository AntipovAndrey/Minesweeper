package minesweeper;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import minesweeper.controllers.LevelsController;

public class Main extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loadMainScene = new FXMLLoader(getClass().getResource("views/LevelsView.fxml"));
        Parent root = loadMainScene.load();
        LevelsController controller = loadMainScene.getController();
        primaryStage.setTitle("Сапер");
        controller.setPrevStage(primaryStage);
        Scene scene = new Scene(root);
        primaryStage.setScene(scene);
        primaryStage.show();
    }
}
