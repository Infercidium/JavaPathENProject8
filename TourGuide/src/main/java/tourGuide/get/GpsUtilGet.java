package tourGuide.get;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import tourGuide.model.Attraction;
import tourGuide.model.VisitedLocation;
import tourGuide.user.User;

import java.util.List;

@Component
public class GpsUtilGet {

    @Value("${gpsUtil.url}")
    private String gpsUtilUrlBase = "http://localhost:8080";

    WebClient gpsClient = WebClient.builder().baseUrl(gpsUtilUrlBase).build();

    public GpsUtilGet() { }

    public List<Attraction> attractionsList() {
        return gpsClient.get().uri("/attractions").retrieve().bodyToFlux(Attraction.class).collectList().block();
    }

    public VisitedLocation visitedLocation(User user) {
        return gpsClient.get().uri("/userLocation/{userID}", user.getUserId().toString()).retrieve().bodyToMono(VisitedLocation.class).block();
    }
}
