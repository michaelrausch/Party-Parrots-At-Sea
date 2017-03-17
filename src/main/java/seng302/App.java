package seng302;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class App extends Application
{
    @Override
    public void start(Stage primaryStage) throws Exception {
        System.out.println(getClass().getResource("/RaceView.fxml"));

        Parent root = FXMLLoader.load(getClass().getResource("/RaceView.fxml"));
        primaryStage.setTitle("RaceVision");
        primaryStage.setScene(new Scene(root));

        seng302.models.OldApp.main(); // Run this to show how positions are updated

        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}


