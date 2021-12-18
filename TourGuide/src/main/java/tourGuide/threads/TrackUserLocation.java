package tourGuide.threads;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import tourGuide.user.User;

import java.util.Locale;
import java.util.concurrent.Callable;

public class TrackUserLocation implements Callable<User> {
    private final User user;

    @Value("${property.gpsUtil.url}")
    private String gpsUtilUrlBase = "http://localhost:8080";

    WebClient gpsClient = WebClient.builder().baseUrl(gpsUtilUrlBase).build();

    public TrackUserLocation(User user) {
        this.user = user;
    }

    @Override
    public User call() throws Exception {
        Locale.setDefault(new Locale("en", "US"));
        gpsClient.get().uri("/userLocation/{userID}", user.getUserId()).accept(MediaType.APPLICATION_JSON)
                .retrieve().bodyToMono(tourGuide.model.VisitedLocation.class)
                .subscribe(data -> {user.addToVisitedLocations(data);});
        return user;
    }
}
