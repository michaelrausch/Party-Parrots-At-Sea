package seng302.models.parsers;

import org.w3c.dom.*;
import seng302.models.mark.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * parse a course xml file
 * Created by Haoming Yin (hyi25) on 16/3/2017
 */
public class CourseParser extends FileParser {

	private Document doc;
	private HashMap<String, Mark> marks = new HashMap<>();

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
	private SingleMark generateSingleMark(Node node) {
		try {
			if (node.getNodeType() == Node.ELEMENT_NODE) {
				Element element = (Element) node;
				String name = element.getElementsByTagName("name").item(0).getTextContent();
				double lat = Double.valueOf(element.getElementsByTagName("latitude").item(0).getTextContent());
				double lon = Double.valueOf(element.getElementsByTagName("longitude").item(0).getTextContent());
				SingleMark singleMark = new SingleMark(name, lat, lon);
				return singleMark;
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
	private void generateGateMarks() {
		ArrayList<GateMark> gateMarks = new ArrayList<>();

		try {
			NodeList nodes = doc.getElementsByTagName("gate");

			for (int i = 0; i < nodes.getLength(); i++) {
				Node node = nodes.item(i);

				if (node.getNodeType() == Node.ELEMENT_NODE) {
					Element element = (Element) node;
					String name = element.getElementsByTagName("name").item(0).getTextContent();
					SingleMark mark1 = generateSingleMark(element.getElementsByTagName("mark").item(0));
					SingleMark mark2 = generateSingleMark(element.getElementsByTagName("mark").item(1));
					GateMark gateMark;
					if (name.equals("Start") || name.equals("Finish"))
						gateMark = new GateMark(name, MarkType.CLOSED_GATE, mark1, mark2, mark1.getLatitude(), mark1.getLongitude());
					else
						gateMark = new GateMark(name, MarkType.OPEN_GATE, mark1, mark2, mark1.getLatitude(), mark1.getLongitude());
					marks.put(name, gateMark);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * generate an arrayList of marks
	 *
	 * @return an arrayList of marks, or null if no gate has been found.
	 */
	private void generateSingleMarks() {
		ArrayList<SingleMark> singleMarks = new ArrayList<>();

		try {
			// find the "marks" tag
			Node node = doc.getElementsByTagName("marks").item(0);
			// iterate all "marks"'s children
			for (Node n = node.getFirstChild(); n != null; n = n.getNextSibling()) {
				// if node's tag name is "mark"
				if (n.getNodeType() == Node.ELEMENT_NODE) {
					Element element = (Element) n;
					if (element.getNodeName() == "mark") {
						Mark mark = generateSingleMark(n);
						marks.put(mark.getName(), mark);
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * return the order of all the marks along a course
	 *
	 * @return an arrayList of the names of ordered course marks
	 */
	private ArrayList<String> getOrder() {
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

	public ArrayList<Mark> getCourse() {
		generateSingleMarks();
		generateGateMarks();
		ArrayList<Mark> course = new ArrayList<>();
		try {
            for (String mark : getOrder()) {
                course.add(marks.get(mark));
            }
        } catch (Exception e) {
		    e.printStackTrace();
        }
		return course;
	}
}
