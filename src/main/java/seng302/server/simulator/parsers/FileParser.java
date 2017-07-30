package seng302.server.simulator.parsers;

import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.InputStream;
import java.io.StringReader;

/**
 * Created by Haoming Yin (hyi25) on 16/3/2017
 */
public abstract class FileParser {

    private String filePath;

    public FileParser() {}

    public FileParser(String path) {
        this.filePath = path;
    }

    protected Document parseFile() {
        try {
            InputStream is = getClass().getResourceAsStream(this.filePath);
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(is);
            // optional, in order to recover info from broken line.
            doc.getDocumentElement().normalize();
            return doc;
        } catch (Exception e) {
            System.out.println("[FileParser] Exception");
            return null;
        }
    }

    protected Document parseFile(String xmlString) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(new InputSource(new StringReader(xmlString)));
            // optional, in order to recover info from broken line.
            doc.getDocumentElement().normalize();
            return doc;
        } catch (Exception e) {
            System.out.println("[FileParser] Exception");
        }
        return null;
    }
}
