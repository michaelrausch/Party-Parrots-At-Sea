package seng302;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import seng302.models.parsers.StreamParser;
import seng302.models.parsers.StreamReceiver;

public class App extends Application
{
    @Override
    public void start(Stage primaryStage) throws Exception {

        Parent root = FXMLLoader.load(getClass().getResource("/views/MainView.fxml"));
        primaryStage.setTitle("RaceVision");
        primaryStage.setScene(new Scene(root));

        primaryStage.show();
//        StreamReceiver sr = new StreamReceiver("csse-s302staff.canterbury.ac.nz", 4941,"TestThread1");
        StreamReceiver sr = new StreamReceiver("livedata.americascup.com", 4941, "TestThread1");
        sr.start();
        StreamParser streamParser = new StreamParser("TestThread2");

        streamParser.start();

    }

    public static void main(String[] args) {
        launch(args);
    }
}


