package tourGuide.proxy;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import tourGuide.model.Attraction;
import tourGuide.model.VisitedLocation;
import tourGuide.user.User;

import java.util.List;

/**
 * Uses gpsUtil Controller.
 */
@Component
public class GpsUtilProxy {

    @Value("${gpsUtil.url}")
    public String gpsUtilUrlBase = "http://localhost:8080";

    WebClient gpsClient = WebClient.builder().baseUrl(gpsUtilUrlBase).build();

    public GpsUtilProxy() { }

    public List<Attraction> attractionsList() {
        return gpsClient.get().uri("/attractions").retrieve().bodyToFlux(Attraction.class).collectList().block();
    }

    public VisitedLocation visitedLocation(User user) {
        return gpsClient.get().uri("/userLocation/{userID}", user.getUserId().toString()).retrieve().bodyToMono(VisitedLocation.class).block();
    }
}
