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
        primaryStage.setMaximized(true);

        primaryStage.show();
        primaryStage.setOnCloseRequest(e -> {
            StreamParser.appClose();
            StreamReceiver.noMoreBytes();
            System.out.println("[CLIENT] Exiting program");
            System.exit(0);
        });

    }

    public static void main(String[] args) {
        StreamReceiver sr = null;

        new ServerThread("Racevision Test Server");

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        if (args.length == 1 && args[0].equals("-standalone")){
            return;
        }

        if (args.length == 3 && args[0].equals("-server")){

            sr = new StreamReceiver(args[1], Integer.valueOf(args[2]), "RaceStream");

        } else if(args.length == 2 && args[0].equals("-server")){
            switch (args[1]) {
                case "internal":
                    sr = new StreamReceiver("localhost", 4949, "RaceStream");
                    break;
                case "staffserver":
                    sr = new StreamReceiver("csse-s302staff.canterbury.ac.nz", 4941, "RaceStream");
                    break;
                case "official":
                    sr = new StreamReceiver("livedata.americascup.com", 4941, "RaceStream");
                    break;
            }
        }
        //Change the StreamReceiver in this else block to change the default data source.
        else{
            sr = new StreamReceiver("livedata.americascup.com", 4941, "RaceStream");
        }

        sr.start();
        StreamParser streamParser = new StreamParser("StreamParser");
        streamParser.start();

        launch(args);



    }
}


