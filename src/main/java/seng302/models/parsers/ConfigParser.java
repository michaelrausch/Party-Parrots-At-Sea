package seng302.models.parsers;


import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import java.util.DoubleSummaryStatistics;

public class ConfigParser extends FileParser {

	private Document doc;

	public ConfigParser(String path) {
		super(path);
		this.doc = this.parseFile();
	}

	/**
	 * Gets wind direction from config file.
	 *
	 * @return a double type degree, or 0 if no value or invalid value is found
	 */
	public double getWindDirection() {
		return getDoubleByTagName("wind-direction", 0.0);
	}

	/**
	 * Gets a non negative time scale for the race
	 *
	 * @return a double type scale, or 0 if no scale or invalid scale is found
	 */
	public double getTimeScale() {
		return getDoubleByTagName("time-scale", 1.0);
	}

	/**
	 * Gets a double type number by given tag name found in xml file
	 *
	 * @param tagName    a string of tag name
	 * @param defaultVal value returned if no value or invalid value is found
	 * @return value found
	 */
	public double getDoubleByTagName(String tagName, double defaultVal) {
		double val = defaultVal;
		try {
			Node node = this.doc.getElementsByTagName(tagName).item(0);
			if (node.getNodeType() == Node.ELEMENT_NODE) {
				Element element = (Element) node;
				val = Double.valueOf(element.getTextContent());
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			return val;
		}
	}

	/**
	 * Gets a string by given tag name found in xml file
	 *
	 * @param tagName    a string of tag name
	 * @param defaultVal a string returned if no value or invalid value is found
	 * @return string found
	 */
	public String getStringByTagName(String tagName, String defaultVal) {
		String string = defaultVal;
		try {
			Node node = this.doc.getElementsByTagName(tagName).item(0);
			if (node.getNodeType() == Node.ELEMENT_NODE) {
				Element element = (Element) node;
				string = element.getTextContent();
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			return string;
		}
	}
}
