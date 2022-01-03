package tourGuide.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import tourGuide.proxy.GpsUtilProxy;
import tourGuide.proxy.RewardCentralProxy;
import tourGuide.model.Attraction;
import tourGuide.model.Location;
import tourGuide.model.VisitedLocation;
import tourGuide.user.User;
import tourGuide.user.UserReward;

import java.util.List;

@Service
public class RewardsService {

    private static final double STATUTE_MILES_PER_NAUTICAL_MILE = 1.15077945;

	// proximity in miles
    private int defaultProximityBuffer = 10;
	private int proximityBuffer = defaultProximityBuffer;
	private int attractionProximityRange = 200;

	@Autowired
	private RewardCentralProxy rewardCentralProxy;

	@Autowired
	private GpsUtilProxy gpsUtilProxy;
	
	public RewardsService() { }
	
	public void setProximityBuffer(int proximityBuffer) {
		this.proximityBuffer = proximityBuffer;
	}

	public void setDefaultProximityBuffer() {
		proximityBuffer = defaultProximityBuffer;
	}

	/**
	 * Compare the user's locations and attractions, the user is synchronized so that the two methods do not interfere.
	 * @param user to verify.
	 */
	public void calculateRewards(User user) {
		synchronized (user) {
			List<VisitedLocation> userLocations = user.getVisitedLocations();

			List<Attraction> attractionList = gpsUtilProxy.attractionsList();

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
	}

	/**
	 * Make a call to rewardCentral.
	 * @param attraction ID.
	 * @param user ID.
	 * @return the number of reward points offered by RewardCentral.
	 */
	private int getRewardPoints(Attraction attraction, User user) {
		return rewardCentralProxy.rewardPoint(attraction, user);
	}

	/**
	 * Compare two distances.
	 * @param visitedLocation : location of the user.
	 * @param attraction : location of the attraction.
	 * @return true if distance is less than the limit otherwise returns false.
	 */
	private boolean nearAttraction(VisitedLocation visitedLocation, Attraction attraction) {
		return getDistance(attraction, visitedLocation.getLocation()) > proximityBuffer ? false : true;
	}

	/**
	 * Compare two locations.
	 * @param loc1 : location 1.
	 * @param loc2 : location 2.
	 * @return the distance in miles between the two locations.
	 */
	public double getDistance(Location loc1, Location loc2) {
        double lat1 = Math.toRadians(loc1.getLatitude());
        double lon1 = Math.toRadians(loc1.getLongitude());
        double lat2 = Math.toRadians(loc2.getLatitude());
        double lon2 = Math.toRadians(loc2.getLongitude());

        double angle = Math.acos(Math.sin(lat1) * Math.sin(lat2)
                               + Math.cos(lat1) * Math.cos(lat2) * Math.cos(lon1 - lon2));

        double nauticalMiles = 60 * Math.toDegrees(angle);
		return STATUTE_MILES_PER_NAUTICAL_MILE * nauticalMiles;
	}
}
