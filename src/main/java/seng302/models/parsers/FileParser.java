package seng302.models.parsers;

import org.w3c.dom.*;
import javax.xml.parsers.*;
import java.io.*;

/**
 *
 *
 * Created by Haoming Yin (hyi25) on 16/3/2017
 */
public abstract class FileParser {

	private String filePath;

	public FileParser(String path) {
		this.filePath = path;
	}

	protected Document parseFile () {
		try {
			File file = new File(this.filePath);
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document doc = builder.parse(file);
			// optional, in order to recover info from broken line.
			doc.getDocumentElement().normalize();
			return doc;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}

	}
}
