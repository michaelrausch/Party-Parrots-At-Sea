package seng302.server.simulator.parsers;

import java.util.ArrayList;
import java.util.List;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import seng302.server.simulator.Boat;
import seng302.server.simulator.mark.Corner;

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

	/**
	 * Parses race.xml file and returns a list of corner which is the race course.
	 * @return a list of ordered corner to represent the course.
	 */
	public List<Corner> getCourse() {
		CourseParser cp = new CourseParser(path);
		return cp.getCourse();
	}

	/**
	 * Parses race.xml file and return a list of boats which will compete in the
	 * race.
	 * @return a list of boats that are going to compete in the race.
	 */
	public List<Boat> getBoats() {
		NodeList yachts = doc.getDocumentElement().getElementsByTagName("Yacht");
		List<Boat> boats = new ArrayList<>();

		for (int i = 0; i < yachts.getLength(); i++) {
			boats.add(getBoat(yachts.item(i)));
		}
		return boats;
	}

	/**
	 * Parses a single boat from the given node
	 * @param node a node within a boat tag
	 * @return a boat instance parsed from the given node
	 */
	private Boat getBoat(Node node) {
		if (node.getNodeType() == Node.ELEMENT_NODE) {
			Element e = (Element) node;

			Integer sourceId = Integer.valueOf(e.getAttribute("SourceID"));
			return new Boat(sourceId, "Test Boat");
		}
		return null;
	}
}
