package tourGuide.dto;

import gpsUtil.location.Attraction;

public class AttractionDto {
    private String name;
    private double latitude;
    private double longitude;
    private double distance;
    private int rewardPoint;

    public AttractionDto(Attraction attraction) {
        this.name = attraction.attractionName;
        this.latitude = attraction.latitude;
        this.longitude = attraction.longitude;
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
