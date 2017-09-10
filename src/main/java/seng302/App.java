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
        ViewManager.getInstance().initialStartView(primaryStage);
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


