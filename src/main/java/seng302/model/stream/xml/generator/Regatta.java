package seng302.model.stream.xml.generator;

/**
 * A Race regatta that can be parsed into XML
 */
public class Regatta {
  private final Double DEFAULT_ALTITUDE = 0d;
  private final Integer DEFAULT_REGATTA_ID = 0;

  private Integer id;
  private String name;
  private String courseName;

  private Double latitude;
  private Double longitude;
  private Double altitude;

  private Integer utcOffset;
  private Double magneticVariation;

  public Regatta(String name, String courseName, Double latitude, Double longitude) {
      this.name = name;
      this.id = DEFAULT_REGATTA_ID;
      this.courseName = courseName;

      this.latitude = latitude;
      this.longitude = longitude;
      this.altitude = DEFAULT_ALTITUDE;

      this.utcOffset = 0;
      this.magneticVariation = 0d;
  }

  public void setMagneticVariation(Double magneticVariation){
      this.magneticVariation = magneticVariation;
  }

  public void setUtcOffset(Integer offset){
    this.utcOffset = offset;
  }

  /*
  NOTE!! The following getters must follow the JavaBean standard (getPropertyName()), and must be public.
   */

  public String getName(){
      return name;
  }

  public String getCourseName(){
      return courseName;
  }

  public Integer getRegattaId(){
      return id;
  }

  public Double getLatitude() {
      return latitude;
  }

  public Double getLongitude() {
      return longitude;
  }

  public Double getAltitude() {
      return altitude;
  }

  public Integer getUtcOffset(){
      return utcOffset;
  }

  public Double getMagneticVariation(){
      return magneticVariation;
  }
}
