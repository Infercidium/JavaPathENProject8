package tourGuide.service;

import gpsUtil.GpsUtil;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import org.springframework.web.reactive.function.client.WebClient;
import tourGuide.constant.ExecutorThreadParam;
import tourGuide.model.Attraction;
import tourGuide.model.Location;
import tourGuide.model.VisitedLocation;
import tourGuide.user.User;
import tourGuide.user.UserReward;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Service
public class RewardsService {

    private static final double STATUTE_MILES_PER_NAUTICAL_MILE = 1.15077945;

	@Value("${gpsUtil.url}")
	private String gpsUtilUrlBase = "http://localhost:8080";

	WebClient gpsClient = WebClient.builder().baseUrl(gpsUtilUrlBase).build();

	@Value("${rewardCentral.url}")
	private String rewardCentralUrlBase = "http://localhost:8080";

	WebClient rewardClient = WebClient.builder().baseUrl(rewardCentralUrlBase).build();

	// proximity in miles
    private int defaultProximityBuffer = 10;
	private int proximityBuffer = defaultProximityBuffer;
	private int attractionProximityRange = 200;
	private ExecutorService executorRewardService = Executors.newFixedThreadPool(ExecutorThreadParam.N_THREADS);
	
	public RewardsService() { }
	
	public void setProximityBuffer(int proximityBuffer) {
		this.proximityBuffer = proximityBuffer;
	}

	public void setDefaultProximityBuffer() {
		proximityBuffer = defaultProximityBuffer;
	}

	public void calculateRewards(User user) {
		List<VisitedLocation> userLocations = user.getVisitedLocations();
		GpsUtil gpsUtil = new GpsUtil();
		System.out.println("Passage dans calculateRewards");
		//TODO Provisoire
		List<gpsUtil.location.Attraction> attractions = gpsUtil.getAttractions();
		List<Attraction> attractionList = new ArrayList<>();
		for (gpsUtil.location.Attraction attraction : attractions) {
			Attraction attraction1 = new Attraction();
			attraction1.setAttractionId(attraction.attractionId);
			attraction1.setAttractionName(attraction.attractionName);
			attraction1.setCity(attraction.city);
			attraction1.setState(attraction.state);
			attraction1.setLatitude(attraction.latitude);
			attraction1.setLongitude(attraction.longitude);
			attractionList.add(attraction1);
		}

		for(VisitedLocation visitedLocation : userLocations) {
			for(Attraction attraction : attractionList) {
				if(user.getUserRewards().stream().filter(r -> r.attraction.getAttractionName().equals(attraction.getAttractionName())).count() == 0) {
					if(nearAttraction(visitedLocation, attraction)) {
						user.addUserReward(new UserReward(visitedLocation, attraction, getRewardPoints(attraction, user)));
					}
				}
			}
		}
	}

	public void calculateRewardsEnd() {
		executorRewardService.shutdown();
		try {
			executorRewardService.awaitTermination(24L, TimeUnit.HOURS);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

	}

	private int getRewardPoints(Attraction attraction, User user) {
		return rewardClient.get().uri("/RewardCentralPoint/{attractionId}/{userId}", attraction.getAttractionId(), user.getUserId()).retrieve().bodyToMono(Integer.class).block();
	}

	private boolean nearAttraction(VisitedLocation visitedLocation, Attraction attraction) {
		return getDistance(attraction, visitedLocation.getLocation()) > proximityBuffer ? false : true;
	}

	public boolean isWithinAttractionProximity(Attraction attraction, Location location) {
		return getDistance(attraction, location) > attractionProximityRange ? false : true;
	}
	
	public double getDistance(Location loc1, Location loc2) {
        double lat1 = Math.toRadians(loc1.getLatitude());
        double lon1 = Math.toRadians(loc1.getLongitude());
        double lat2 = Math.toRadians(loc2.getLatitude());
        double lon2 = Math.toRadians(loc2.getLongitude());

        double angle = Math.acos(Math.sin(lat1) * Math.sin(lat2)
                               + Math.cos(lat1) * Math.cos(lat2) * Math.cos(lon1 - lon2));

        double nauticalMiles = 60 * Math.toDegrees(angle);
        double statuteMiles = STATUTE_MILES_PER_NAUTICAL_MILE * nauticalMiles;
        return statuteMiles;
	}
}
