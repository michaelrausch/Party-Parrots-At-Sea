package seng302.server.simulator.parsers;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import seng302.server.simulator.Boat;
import seng302.server.simulator.mark.Corner;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Parses the race xml file to get course details
 * Created by Haoming Yin (hyi25) on 16/3/2017
 */
public class RaceParser extends FileParser {

	private Document doc;
	private String path;

	public RaceParser(String path) {
		super(path);
		this.path = path;
		this.doc = this.parseFile();
	}

	public List<Corner> getCourse() {
		CourseParser cp = new CourseParser(path);
		return cp.getCourse();
	}

	public List<Boat> getBoats() {
		NodeList yachts = doc.getDocumentElement().getElementsByTagName("Yacht");
		List<Boat> boats = new ArrayList<>();

		for (int i = 0; i < yachts.getLength(); i++) {
			boats.add(getBoat(yachts.item(i)));
		}
		return boats;
	}

	private Boat getBoat(Node node) {
		if (node.getNodeType() == Node.ELEMENT_NODE) {
			Element e = (Element) node;

			Integer sourceId = Integer.valueOf(e.getAttribute("SourceID"));
			return new Boat(sourceId, "Test Boat");
		}
		return null;
	}
}
