package seng302;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import seng302.model.PolarTable;

public class App extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        PolarTable.parsePolarFile(getClass().getResourceAsStream("/config/acc_polars.csv"));

        Parent root = FXMLLoader.load(getClass().getResource("/views/StartScreenView.fxml"));
        primaryStage.setTitle("RaceVision");
        Scene scene = new Scene(root, 1530, 960);
        scene.getStylesheets().add(getClass().getResource("/css/master.css").toString());
        primaryStage.setScene(scene);
//        primaryStage.setMaxWidth(1530);
//        primaryStage.setMaxHeight(960);
        primaryStage.getIcons().add(new Image(getClass().getResourceAsStream("/PP.png")));
//        primaryStage.setMaximized(true);

        primaryStage.show();
        primaryStage.setOnCloseRequest(e -> {
//            ClientPacketParser.appClose();
//            ClientPacketParser.appClose();
            System.exit(0);
        });

//        ClientState.primaryStage = primaryStage;
    }

    public static void main(String[] args) {
        launch(args);
    }
}


