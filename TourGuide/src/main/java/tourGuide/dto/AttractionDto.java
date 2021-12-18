package tourGuide.dto;


import tourGuide.model.Attraction;

public class AttractionDto {
    private String name;
    private double latitude;
    private double longitude;
    private double distance;
    private int rewardPoint;

    public AttractionDto(Attraction attraction) {
        this.name = attraction.getAttractionName();
        this.latitude = attraction.getLatitude();
        this.longitude = attraction.getLongitude();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getDistance() {
        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    public int getRewardPoint() {
        return rewardPoint;
    }

    public void setRewardPoint(int rewardPoint) {
        this.rewardPoint = rewardPoint;
    }
}
