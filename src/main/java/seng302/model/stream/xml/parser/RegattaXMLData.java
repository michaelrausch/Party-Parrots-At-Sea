package seng302.model.stream.xml.parser;

/**
 * Stores data from regatta xml packet.
 */
public class RegattaXMLData {
    //Regatta Info
    private Integer regattaID;
    private String regattaName;
    private String courseName;
    private Double centralLat;
    private Double centralLng;
    private Integer utcOffset;

    RegattaXMLData (Integer regattaID, String regattaName, String courseName,
        Double centralLat, Double centralLng, Integer utcOffset) {
        this.regattaID = regattaID;
        this.regattaName = regattaName;
        this.courseName = courseName;
        this.centralLat = centralLat;
        this.centralLng = centralLng;
        this.utcOffset = utcOffset;
    }

    public Integer getRegattaID() {
        return regattaID;
    }

    public String getRegattaName() {
        return regattaName;
    }

    public String getCourseName() {
        return courseName;
    }

    public Double getCentralLat() {
        return centralLat;
    }

    public Double getCentralLng() {
        return centralLng;
    }

    public Integer getUtcOffset() {
        return utcOffset;
    }

}
