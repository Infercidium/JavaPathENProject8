package tourGuide.model;

import java.util.UUID;

/**
 * Model of gpsUtil.
 */
public class Attraction extends Location {
    private String attractionName;
    private String city;
    private String state;
    private UUID attractionId;

    public Attraction() {
    }

    public Attraction(final String attractionName, final String city, final String state,
                      final double latitude, final double longitude) {
        super(latitude, longitude);
        this.attractionName = attractionName;
        this.city = city;
        this.state = state;
        this.attractionId = UUID.randomUUID();
    }

    public String getAttractionName() {
        return attractionName;
    }

    public void setAttractionName(String attractionName) {
        this.attractionName = attractionName;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public UUID getAttractionId() {
        return attractionId;
    }

    public void setAttractionId(UUID attractionId) {
        this.attractionId = attractionId;
    }
}
