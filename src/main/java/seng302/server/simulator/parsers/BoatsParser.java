package seng302.server.simulator.parsers;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;


/**
 * Parses the race xml file to get course details
 * Created by Haoming Yin (hyi25) on 16/3/2017
 */
public class BoatsParser extends FileParser {

	private Document doc;

	public BoatsParser(String path) {
		super(path);
		this.doc = this.parseFile();
	}

}
