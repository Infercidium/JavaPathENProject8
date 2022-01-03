package tourGuide.proxy;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import tourGuide.model.Provider;
import tourGuide.user.User;

import java.util.List;

/**
 * Uses TripPricer Controller.
 */
@Component
public class TripPricerProxy {

    @Value("${tripPricer.url}")
    public String tripPricerUrlBase;

    @Autowired
    private WebClient pricerClient;

    public TripPricerProxy() { }

    public List<Provider> price(String apiKey, User user, int rewardsPoints) {
        return pricerClient.get()
                .uri("/price/{apiKey}/{attractionId}/{adult}/{children}/}{nightsStay}/{rewardsPoints}", apiKey, user.getUserId(),
                        user.getUserPreferences().getNumberOfAdults(), user.getUserPreferences().getNumberOfChildren(), user.getUserPreferences().getTripDuration(), rewardsPoints)
                .retrieve().bodyToFlux(Provider.class).collectList().block();
    }
}
