package tourGuide.get;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.reactive.function.client.WebClient;
import tourGuide.model.Attraction;
import tourGuide.user.User;

public class RewardCentralGet {

    @Value("${rewardCentral.url}") //TODO Faire fonnctionner correctement
    private String rewardCentralUrlBase = "http://localhost:8080";

    WebClient rewardClient = WebClient.builder().baseUrl(rewardCentralUrlBase).build();

    public RewardCentralGet() { }

    public int rewardPoint(Attraction attraction, User user) {
        return rewardClient.get().uri("/RewardCentralPoint/{attractionId}/{userId}", attraction.getAttractionId(), user.getUserId()).retrieve().bodyToMono(Integer.class).block();
    }
}
