package unitaire.proxy;

import com.jsoniter.output.JsonStream;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.reactive.function.client.WebClient;
import tourGuide.helper.InternalTestHelper;
import tourGuide.model.Attraction;
import tourGuide.model.Location;
import tourGuide.model.Provider;
import tourGuide.model.VisitedLocation;
import tourGuide.proxy.GpsUtilProxy;
import tourGuide.user.User;
import tourGuide.user.UserReward;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.isA;
import static org.mockito.Mockito.when;

@SpringBootTest(classes = {GpsUtilProxy.class})
@RunWith(SpringRunner.class)
public class GpsUtilProxyTest {

    @MockBean
    WebClient gpsClient;

    GpsUtilProxy gpsUtilProxy = new GpsUtilProxy(gpsClient);

    UUID uuid = UUID.randomUUID();
    User user = new User(uuid, "name", "phone", "mail");
    Location location = new Location(10, 10);
    VisitedLocation visitedLocation = new VisitedLocation(uuid, location, new Date());
    Attraction attraction = new Attraction("attraction", "city", "state", 10, 10);

    @Before
    public void setUp() {
        user.addToVisitedLocations(visitedLocation);
    }

    @Test
    public void attractionsList() {
        /*when(gpsClient.get().uri("/attractions").retrieve().bodyToFlux(Attraction.class).collectList().block())
                .thenReturn(Collections.singletonList(attraction));*/
        List<Attraction> result = gpsUtilProxy.attractionsList();
        assertEquals(1, result.size());
    }

    @Test
    public void visitedLocation() {
       /* when(gpsClient.get().uri("/userLocation/{userID}", user.getUserId().toString()).retrieve().bodyToMono(VisitedLocation.class).block())
                .thenReturn(visitedLocation);*/
        VisitedLocation result = gpsUtilProxy.visitedLocation(user);
        assertEquals(JsonStream.serialize(visitedLocation), JsonStream.serialize(result));
    }
}