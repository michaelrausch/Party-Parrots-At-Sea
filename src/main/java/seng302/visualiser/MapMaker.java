package seng302.visualiser;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javafx.scene.Node;
import javafx.util.Pair;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import seng302.model.stream.xml.generator.RaceXMLTemplate;
import seng302.model.stream.xml.generator.RegattaXMLTemplate;
import seng302.model.stream.xml.parser.RaceXMLData;
import seng302.model.stream.xml.parser.RegattaXMLData;
import seng302.utilities.XMLGenerator;
import seng302.utilities.XMLParser;

/**
 * Makes maps from map definition xml files.
 */
public class MapMaker {

    private List<MapPreview> mapPreviews = new ArrayList<>();
    private List<RaceXMLData> races = new ArrayList<>();
    private List<RegattaXMLData> regattas = new ArrayList<>();
    private List<String> filePaths = new ArrayList<>();
    private List<Integer> maxPlayers = new ArrayList<>();
    private static MapMaker instance;
    private int index = 0;
    private XMLGenerator xmlGenerator = new XMLGenerator();

    private List<String> maps = new ArrayList<>(
        Arrays.asList("default.xml", "horseshoe.xml", "loop.xml", "madagascar.xml", "waiheke.xml"));

    public static MapMaker getInstance() {
        if (instance == null) {
            instance = new MapMaker();
        }
        return instance;
    }

    private MapMaker() {
        for (String mapPath : maps){
            String path = ("/maps/" + mapPath);

            Pair<RegattaXMLTemplate, RaceXMLTemplate> regattaRace = XMLParser.parseRaceDef(
                    path, "", 1, null, false
            );

            RegattaXMLTemplate regattaTemplate = regattaRace.getKey();

            filePaths.add(path);

            regattas.add(new RegattaXMLData(
                    regattaTemplate.getRegattaId(),
                    regattaTemplate.getName(),
                    regattaTemplate.getCourseName(),
                    regattaTemplate.getLatitude(),
                    regattaTemplate.getLongitude(),
                    regattaTemplate.getUtcOffset()
            ));

            RaceXMLTemplate raceTemplate = regattaRace.getValue();
            xmlGenerator.setRaceTemplate(raceTemplate);
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db;
            Document doc = null;
            try {
                db = dbf.newDocumentBuilder();
                doc = db.parse(new InputSource(new StringReader(xmlGenerator.getRaceAsXml())));
            } catch (ParserConfigurationException | IOException | SAXException e) {
                e.printStackTrace();
            }

            RaceXMLData race = XMLParser.parseRace(doc);
            maxPlayers.add(XMLParser.getMaxPlayers(doc));

            mapPreviews.add(new MapPreview(
                    new ArrayList<>(race.getCompoundMarks().values()),
                    race.getMarkSequence(), race.getCourseLimit()
            ));

            races.add(race);
        }

    }

    public void next() {
        index += 1;
        if (index >= mapPreviews.size()) {
            index = 0;
        }
    }

    public void previous() {
        index -= 1;
        if (index < 0) {
            index = mapPreviews.size() - 1;
        }
    }

    public Node getCurrentGameView() {
        return mapPreviews.get(index).getAssets();
    }

    public RaceXMLData getCurrentRace() {
        return races.get(index);
    }

    public RegattaXMLData getCurrentRegatta() {
        return regattas.get(index);
    }

    public String getCurrentRacePath() {
        return filePaths.get(index);
    }

    public Integer getMaxPlayers() {
        return maxPlayers.get(index);
    }
}
