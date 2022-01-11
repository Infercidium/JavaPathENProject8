package tourGuide.proxy;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import tourGuide.model.Attraction;
import tourGuide.user.User;

/**
 * Uses RewardCentral Controller.
 */
@Component
public class RewardCentralProxy {

    @Value("${rewardCentral.url}")
    public String rewardCentralUrlBase;

    @Autowired
    private WebClient rewardClient;

    public RewardCentralProxy() { }

    public int rewardPoint(Attraction attraction, User user) {
        return rewardClient.get().uri("/RewardCentralPoint/{attractionId}/{userId}", attraction.getAttractionId(), user.getUserId()).retrieve().bodyToMono(Integer.class).block();
    }
}
