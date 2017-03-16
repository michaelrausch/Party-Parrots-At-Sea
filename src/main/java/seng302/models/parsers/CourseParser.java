package seng302.models.parsers;

import org.w3c.dom.*;
import seng302.models.GateMark;
import seng302.models.Mark;

import java.util.ArrayList;
import java.util.NoSuchElementException;

/**
 * parse a course xml file
 * Created by Haoming Yin (hyi25) on 16/3/2017
 */
public class CourseParser extends FileParser {

	private Document doc;

	public CourseParser(String path) {
		super(path);
		this.doc = this.parseFile();
	}

	/**
	 * create a mark by given node
	 *
	 * @param node
	 * @return a mark, or null if fails to create a mark
	 */
	private Mark generateMark(Node node) {
		try {
			if (node.getNodeType() == Node.ELEMENT_NODE) {
				Element element = (Element) node;
				String name = element.getElementsByTagName("name").item(0).getTextContent();
				double lat = Double.valueOf(element.getElementsByTagName("latitude").item(0).getTextContent());
				double lon = Double.valueOf(element.getElementsByTagName("longitude").item(0).getTextContent());
				Mark mark = new Mark(name, lat, lon);
				return mark;
			} else {
				throw new NoSuchElementException("Cannot generate a mark by given node.");
			}
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * generate an arrayList of gates
	 *
	 * @return an arrayList of gates, or null if no gate has been found.
	 */
	public ArrayList<GateMark> getGates() {
		ArrayList<GateMark> gateMarks = new ArrayList<>();

		try {
			NodeList nodes = doc.getElementsByTagName("gate");

			for (int i = 0; i < nodes.getLength(); i++) {
				Node node = nodes.item(i);

				if (node.getNodeType() == Node.ELEMENT_NODE) {
					Element element = (Element) node;
					String name = element.getElementsByTagName("name").item(0).getTextContent();
					Mark mark1 = generateMark(element.getElementsByTagName("mark").item(0));
					Mark mark2 = generateMark(element.getElementsByTagName("mark").item(1));
					GateMark gateMark = new GateMark(name, mark1, mark2);
					gateMarks.add(gateMark);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return gateMarks;
	}

	/**
	 * generate an arrayList of marks
	 *
	 * @return an arrayList of marks, or null if no gate has been found.
	 */
	public ArrayList<Mark> getMarks() {
		ArrayList<Mark> marks = new ArrayList<>();

		try {
			// find the "marks" tag
			Node node = doc.getElementsByTagName("marks").item(0);
			// iterate all "marks"'s children
			for (Node n = node.getFirstChild(); n != null; n = n.getNextSibling()) {
				// if node's tag name is "mark"
				if (n.getNodeType() == Node.ELEMENT_NODE) {
					Element element = (Element) n;
					if (element.getNodeName() == "mark") {
						marks.add(generateMark(n));
					}
				}
			}
			return marks;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * return the order of all the marks along a course
	 *
	 * @return an arrayList of the names of ordered course marks
	 */
	public ArrayList<String> getOrder() {
		ArrayList<String> markOrder = new ArrayList<>();

		try {
			Node orderNode = doc.getElementsByTagName("order").item(0);
			for (Node node = orderNode.getFirstChild(); node != null; node = node.getNextSibling()) {
				if (node.getNodeType() == Node.ELEMENT_NODE) {
					Element element = (Element) node;
					String name = element.getTextContent();
					markOrder.add(name);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return markOrder;
	}
}
