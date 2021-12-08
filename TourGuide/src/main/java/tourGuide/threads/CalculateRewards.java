package tourGuide.threads;

import gpsUtil.GpsUtil;
import gpsUtil.location.Attraction;
import gpsUtil.location.VisitedLocation;
import rewardCentral.RewardCentral;
import tourGuide.service.RewardsService;
import tourGuide.user.User;
import tourGuide.user.UserReward;

import java.util.List;

public class CalculateRewards implements Runnable {
    private final GpsUtil gpsUtil;
    private final RewardCentral rewardCentral;
    private final RewardsService rewardsService;
    private final int proximityBuffer;
    private final User user;

    public CalculateRewards(GpsUtil gpsUtil, RewardCentral rewardCentral, RewardsService rewardsService, User user, int proximityBuffer) {
        this.gpsUtil = gpsUtil;
        this.rewardCentral = rewardCentral;
        this.rewardsService = rewardsService;
        this.user = user;
        this.proximityBuffer = proximityBuffer;
    }

    @Override
    public void run() {
        List<VisitedLocation> userLocations = user.getVisitedLocations();
        List<Attraction> attractions = gpsUtil.getAttractions();

        for(VisitedLocation visitedLocation : userLocations) {
            for(Attraction attraction : attractions) {
                if(user.getUserRewards().stream().filter(r -> r.attraction.attractionName.equals(attraction.attractionName)).count() == 0) {
                    if(nearAttraction(visitedLocation, attraction)) {
                        user.addUserReward(new UserReward(visitedLocation, attraction, getRewardPoints(attraction, user)));
                    }
                }
            }
        }
    }

    private int getRewardPoints(Attraction attraction, User user) {
        return rewardCentral.getAttractionRewardPoints(attraction.attractionId, user.getUserId());
    }

    private boolean nearAttraction(VisitedLocation visitedLocation, Attraction attraction) {
        return rewardsService.getDistance(attraction, visitedLocation.location) > proximityBuffer ? false : true;
    }
}
