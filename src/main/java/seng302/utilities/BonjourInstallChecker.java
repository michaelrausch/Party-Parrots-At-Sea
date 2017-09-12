package seng302.utilities;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;

public class BonjourInstallChecker {
    private static String INSTALL_URL = "https://support.apple.com/kb/DL999?locale=en_US";
    private static String[] INSTALL_DIRECTORIES = {"C:/Program Files/Bonjour", "C:/Program Files (x86)/Bonjour"};

    private static Boolean isWindows(){
        return  System.getProperty("os.name").startsWith("Windows");
    }

    private static Boolean isBonjourInstalled(){
        for (String dir : INSTALL_DIRECTORIES){
            File file = new File(dir);

            if (file.isDirectory()){
                return true;
            }
        }

        return false;
    }

    public static Boolean isBonjourSupported(){
        if (isWindows()){
            return isBonjourInstalled();
        }

        return true;
    }

    public static void openInstallUrl(){
        Runtime rt = Runtime.getRuntime();

        try {
            rt.exec("rundll32 url.dll,FileProtocolHandler " + INSTALL_URL);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
