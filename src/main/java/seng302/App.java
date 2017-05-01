package seng302;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import seng302.models.parsers.StreamParser;
import seng302.models.parsers.StreamReceiver;
import seng302.server.ServerThread;

public class App extends Application
{
    @Override
    public void start(Stage primaryStage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("/views/MainView.fxml"));
        primaryStage.setTitle("RaceVision");
        primaryStage.setScene(new Scene(root));

        primaryStage.show();
    }

    public static void main(String[] args) {
        StreamReceiver sr;

        new ServerThread("Racevision Test Server");
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        if (args.length > 1){
            sr = new StreamReceiver("localhost", 8085, "RaceStream");
        }
        else{
              sr = new StreamReceiver("csse-s302staff.canterbury.ac.nz", 4941,"RaceStream");
//            sr = new StreamReceiver("livedata.americascup.com", 4941, "RaceStream");
//            sr = new StreamReceiver("localhost", 8085, "RaceStream");
        }

        sr.start();
        StreamParser streamParser = new StreamParser("StreamParser");
        streamParser.start();

        launch(args);
    }
}


