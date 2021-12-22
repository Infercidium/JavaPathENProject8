package tourGuide.get;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.reactive.function.client.WebClient;
import tourGuide.model.Provider;
import tourGuide.user.User;

import java.util.List;

public class TripPricerGet {
    @Value("${tripPricer.url}")
    private String tripPricerUrlBase = "http://localhost:8080";

    WebClient pricerClient = WebClient.builder().baseUrl(tripPricerUrlBase).build();

    public TripPricerGet() { }

    public List<Provider> price(String apiKey, User user, int rewardsPoints) {
        return pricerClient.get()
                .uri("/price/{apiKey}/{attractionId}/{adult}/{children}/}{nightsStay}/{rewardsPoints}", apiKey, user.getUserId(),
                        user.getUserPreferences().getNumberOfAdults(), user.getUserPreferences().getNumberOfChildren(), user.getUserPreferences().getTripDuration(), rewardsPoints)
                .retrieve().bodyToFlux(Provider.class).collectList().block();
    }
}
