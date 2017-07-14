package seng302;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import seng302.models.PolarTable;
import seng302.models.stream.StreamParser;
import seng302.models.stream.StreamReceiver;
import seng302.server.ServerThread;

public class App extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        PolarTable.parsePolarFile(getClass().getResourceAsStream("/config/acc_polars.csv"));

        Parent root = FXMLLoader.load(getClass().getResource("/views/MainView.fxml"));
        primaryStage.setTitle("RaceVision");
        primaryStage.setScene(new Scene(root, 1530, 960));
        primaryStage.setMaxWidth(1530);
        primaryStage.setMaxHeight(960);
//        primaryStage.setMaximized(true);

        primaryStage.show();
        primaryStage.setOnCloseRequest(e -> {
            StreamParser.appClose();
            StreamReceiver.noMoreBytes();
            System.exit(0);
        });

    }

    public static void main(String[] args) {
        launch(args);
    }
}


