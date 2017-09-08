package seng302;

import ch.qos.logback.classic.Level;
import com.jfoenix.controls.JFXDecorator;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import org.apache.commons.cli.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import seng302.visualiser.controllers.ViewManager;
import seng302.gameServer.ServerAdvertiser;

import java.io.IOException;

public class App extends Application {

    private static Logger logger = LoggerFactory.getLogger(App.class);

    public static void parseArgs(String[] args) throws ParseException {
        Options options = new Options();
        CommandLineParser parser = new DefaultParser();
        CommandLine cmd;

        ch.qos.logback.classic.Logger rootLogger = (ch.qos.logback.classic.Logger) LoggerFactory
            .getLogger(Logger.ROOT_LOGGER_NAME);

        options.addOption("debugLevel", true, "Set the application debug level");

        cmd = parser.parse(options, args);

        if (cmd.hasOption("debugLevel")) {

            switch (cmd.getOptionValue("debugLevel")) {
                case "DEBUG":
                    rootLogger.setLevel(Level.DEBUG);
                    break;

                case "ALL":
                    rootLogger.setLevel(Level.ALL);
                    break;

                case "WARNING":
                    rootLogger.setLevel(Level.WARN);
                    break;

                case "ERROR":
                    rootLogger.setLevel(Level.ERROR);
                    break;

                case "INFO":
                    rootLogger.setLevel(Level.INFO);

                case "TRACE":
                    rootLogger.setLevel(Level.TRACE);

                default:
                    rootLogger.setLevel(Level.ALL);
            }
        } else {
            rootLogger.setLevel(Level.WARN);
        }
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("/views/StartScreenView.fxml"));
        primaryStage.setTitle("Party Parrots At Sea");

        JFXDecorator decorator = new JFXDecorator(primaryStage, root,false, true, true);
        decorator.setCustomMaximize(true);
        decorator.applyCss();
        decorator.getStylesheets()
                .add(getClass().getResource("/css/master.css").toExternalForm());

        ViewManager.getInstance().setDecorator(decorator);

        primaryStage.getIcons().add(new Image(getClass().getResourceAsStream("/PP.png")));
        Scene scene = new Scene(decorator, 1200, 800);
        primaryStage.setMinHeight(800);
        primaryStage.setMinWidth(1200);
        primaryStage.setScene(scene);
        primaryStage.show();

        primaryStage.setOnCloseRequest(e -> closeAll());

        decorator.setOnCloseButtonAction(this::closeAll);
    }

    private void closeAll(){
        try {
            ServerAdvertiser.getInstance().unregister();
        } catch (IOException e1) {
            logger.warn("Could not un-register game");
        }

        System.exit(0);
    }

    public static void main(String[] args) {
        try {
            parseArgs(args);
        } catch (ParseException e) {
            logger.error("Could not parse command line arguments");
        }

        launch(args);
    }
}


