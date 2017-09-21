package seng302;

import ch.qos.logback.classic.Level;
import javafx.application.Application;
import javafx.stage.Stage;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import seng302.discoveryServer.DiscoveryServer;
import seng302.visualiser.controllers.ViewManager;

public class App extends Application {

    private static Logger logger = LoggerFactory.getLogger(App.class);
    private static boolean isRunningAsCache = false;

    public static void parseArgs(String[] args) throws ParseException {
        Options options = new Options();
        CommandLineParser parser = new DefaultParser();
        CommandLine cmd;

        ch.qos.logback.classic.Logger rootLogger = (ch.qos.logback.classic.Logger) LoggerFactory
            .getLogger(Logger.ROOT_LOGGER_NAME);

        options.addOption("debugLevel", true, "Set the application debug level");
        options.addOption("runAsDiscoveryServer", false, "Run as a discovery server");
        options.addOption("discoveryDevMode", false, "Use a local discovery server");

        cmd = parser.parse(options, args);

        if (cmd.hasOption("runAsDiscoveryServer")){
            isRunningAsCache = true;
            rootLogger.setLevel(Level.ALL);
            return;
        }

        if (cmd.hasOption("discoveryDevMode")) {
            DiscoveryServer.DISCOVERY_SERVER = "localhost";
        }

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


    public static void main(String[] args) throws Exception {
        try {
            parseArgs(args);
        } catch (ParseException e) {
            logger.error("Could not parse command line arguments");
        }

        if (!isRunningAsCache){
            launch(args);
        }
        else{
            new DiscoveryServer();
        }
    }
}


