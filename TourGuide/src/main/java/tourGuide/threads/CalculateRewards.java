package tourGuide.threads;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import rewardCentral.RewardCentral;
import tourGuide.model.Attraction;
import tourGuide.model.VisitedLocation;
import tourGuide.service.RewardsService;
import tourGuide.user.User;
import tourGuide.user.UserReward;

import java.util.ArrayList;
import java.util.List;

public class CalculateRewards implements Runnable {
    private final RewardCentral rewardCentral;
    private final RewardsService rewardsService;
    private final int proximityBuffer;
    private final User user;

    @Value("${property.gpsUtil.url}")
    private String gpsUtilUrlBase = "http://localhost:8080";

    WebClient gpsClient = WebClient.builder().baseUrl(gpsUtilUrlBase).build();

    public CalculateRewards(RewardCentral rewardCentral, RewardsService rewardsService, User user, int proximityBuffer) {
        this.rewardCentral = rewardCentral;
        this.rewardsService = rewardsService;
        this.user = user;
        this.proximityBuffer = proximityBuffer;

    }

    @Override
    public void run() {
        List<VisitedLocation> userLocations = user.getVisitedLocations();

        Flux<List<Attraction>> attractionFlux = gpsClient.get().uri("/attractions").accept(MediaType.APPLICATION_JSON).retrieve()
                .bodyToFlux(new ParameterizedTypeReference<List<Attraction>>() {});
        List<Attraction> attractions = attractionFlux.blockLast();

        for(VisitedLocation visitedLocation : userLocations) {
            for(Attraction attraction : attractions) {
                if(user.getUserRewards().stream().filter(r -> r.attraction.getAttractionName().equals(attraction.getAttractionName())).count() == 0) {
                    if(nearAttraction(visitedLocation, attraction)) {
                        user.addUserReward(new UserReward(visitedLocation, attraction, getRewardPoints(attraction, user)));
                    }
                }
            }
        }
    }

    private int getRewardPoints(Attraction attraction, User user) {
        return rewardCentral.getAttractionRewardPoints(attraction.getAttractionId(), user.getUserId());
    }

    private boolean nearAttraction(VisitedLocation visitedLocation, Attraction attraction) {
        return rewardsService.getDistance(attraction, visitedLocation.getLocation()) > proximityBuffer ? false : true;
    }
}
