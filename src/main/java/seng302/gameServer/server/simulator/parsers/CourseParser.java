package seng302.gameServer.server.simulator.parsers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import seng302.model.mark.CompoundMark;
import seng302.gameServer.server.simulator.Corner;
import seng302.model.mark.Mark;
import seng302.gameServer.server.simulator.RoundingType;

/**
 * Parses the race xml file to get course details
 * Created by Haoming Yin (hyi25) on 16/3/2017
 */
public class CourseParser extends FileParser {

	private Document doc;
	private Map<Integer, CompoundMark> compoundMarksMap;

	public CourseParser(String path) {
		super(path);
		this.doc = this.parseFile();
	}

	// TODO: should handle error / invalid file gracefully
	protected List<Corner> getCourse() {
		compoundMarksMap = getCompoundMarks(doc.getDocumentElement());
		List<Corner> corners = new ArrayList<>();
		NodeList cMarksSequence = doc.getElementsByTagName("Corner");

		for (int i = 0; i < cMarksSequence.getLength(); i++) {
			corners.add(getCorner(cMarksSequence.item(i)));
		}
		return corners;
	}


	private Corner getCorner(Node node) {
		if (node.getNodeType() == Node.ELEMENT_NODE) {
			Element e = (Element) node;

			Integer seqId = Integer.valueOf(e.getAttribute("SeqID"));
			Integer cMarkId = Integer.valueOf(e.getAttribute("CompoundMarkID"));
			CompoundMark cMark = compoundMarksMap.get(cMarkId);
			RoundingType roundingType = RoundingType.typeOf(e.getAttribute("Rounding"));
			Integer zoneSize = Integer.valueOf(e.getAttribute("ZoneSize"));

			return new Corner(seqId, cMark, roundingType, zoneSize);
		}
		return null;
	}

	private Map<Integer, CompoundMark> getCompoundMarks(Node node) {
		Map<Integer, CompoundMark> compoundMarksMap = new HashMap<>();

		if (node.getNodeType() == Node.ELEMENT_NODE) {
			Element element = (Element) node;
			NodeList cMarks = element.getElementsByTagName("CompoundMark");

			// loop through all compound marks who are the children of course node
			for (int i = 0; i < cMarks.getLength(); i++) {
				CompoundMark cMark = getCompoundMark(cMarks.item(i));
				if (cMark != null)
					compoundMarksMap.put(cMark.getId(), cMark);
			}

			return compoundMarksMap;
		}
		return null;
	}


	private CompoundMark getCompoundMark(Node node) {
		if (node.getNodeType() == Node.ELEMENT_NODE) {
			Element e = (Element) node;
			Integer markID = Integer.valueOf(e.getAttribute("CompoundMarkID"));

			String name = e.getAttribute("Name");
			CompoundMark cMark = new CompoundMark(markID, name);

			NodeList marks = e.getElementsByTagName("Mark");
			for (int i = 0; i < marks.getLength(); i++) {
				Mark mark = getMark(marks.item(i));
				if (mark != null)
					cMark.addSubMarks(mark);
			}
			return cMark;
		}
		System.out.println("Failed to create compound mark.");
		return null;
	}


	private Mark getMark(Node node) {
		if (node.getNodeType() == Node.ELEMENT_NODE) {
			Element e = (Element) node;
			Integer seqId = Integer.valueOf(e.getAttribute("SeqID"));
			String name = e.getAttribute("Name");
			Double lat = Double.valueOf(e.getAttribute("TargetLat"));
			Double lng = Double.valueOf(e.getAttribute("TargetLng"));
			Integer sourceId = Integer.valueOf(e.getAttribute("SourceID"));

			Mark mark = new Mark(name, lat, lng, sourceId);
			mark.setSeqID(seqId);

			return mark;
		}
		System.out.println("Failed to create mark.");
		return null;
	}

}
