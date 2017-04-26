package seng302.models.parsers;

import org.w3c.dom.*;
import seng302.models.Boat;

import java.util.ArrayList;
import java.util.NoSuchElementException;

public class TeamsParser extends FileParser {

	private Document doc;

	public TeamsParser(String path) {
		super(path);
		this.doc = this.parseFile();
	}

	/**
	 * Create a boat instance by a given team node
	 * @param node a boat node containing name, alias and velocity
	 * @return an instance of Boat
	 */
	private Boat parseBoat(Node node) {
		try {
			if (node.getNodeType() == Node.ELEMENT_NODE) {
				Element element = (Element) node;
				String name = element.getElementsByTagName("name").item(0).getTextContent();
				String alias = element.getElementsByTagName("alias").item(0).getTextContent();
				double velocity = Double.valueOf(element.getElementsByTagName("velocity").item(0).getTextContent());
				int id = Integer.valueOf(element.getElementsByTagName("id").item(0).getTextContent());
				Boat boat = new Boat(name, velocity, alias, id);
				return boat;
			} else {
				throw new NoSuchElementException("Cannot generate a boat by given node");
			}
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Create an arraylist of boats instance.
	 * @return an arraylist of boats in teams file
	 */
	public ArrayList<Boat> getBoats() {
		ArrayList<Boat> boats = new ArrayList<>();

		try {
			NodeList nodes = this.doc.getElementsByTagName("team");
			for (int i = 0; i < nodes.getLength(); i++) {
				Node node = nodes.item(i);
				boats.add(parseBoat(node));
			}
			return boats;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}


}

