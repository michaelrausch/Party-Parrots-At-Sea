package seng302.model.stream.parser;

/**
 * Stores parsed data from yacht event code packet
 */
public class YachtEventData {
    private Long subjectId;
    private Long incidentId;
    private Integer eventId;
    private Long timeStamp;

    public YachtEventData(Long subjectId, Long incidentId, Integer eventId, Long timeStamp) {
        this.subjectId = subjectId;
        this.incidentId = incidentId;
        this.eventId = eventId;
        this.timeStamp = timeStamp;
    }

    public Long getSubjectId() {
        return subjectId;
    }

    public Long getIncidentId() {
        return incidentId;
    }

    public Integer getEventId() {
        return eventId;
    }

    public Long getTimeStamp() {
        return timeStamp;
    }
}
