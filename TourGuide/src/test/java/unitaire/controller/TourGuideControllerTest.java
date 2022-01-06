package unitaire.controller;

import com.jsoniter.output.JsonStream;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import tourGuide.controller.TourGuideController;
import tourGuide.model.Location;
import tourGuide.model.VisitedLocation;
import tourGuide.service.RewardsService;
import tourGuide.service.TourGuideService;
import tourGuide.user.User;

import java.util.Date;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

@SpringBootTest(classes = {TourGuideController.class})
public class TourGuideControllerTest {

    @MockBean
    TourGuideService tourGuideService;

    TourGuideController tourGuideController; //= new TourGuideController(tourGuideService);

    @Test
    public void index() {
        String result = tourGuideController.index();
        assertEquals("Greetings from TourGuide!", result);
    }

    @Test //TODO RIen ne marche
    public void getLocation() {
        /*Location location = new Location(10, 10);
        VisitedLocation visitedLocation = new VisitedLocation(UUID.randomUUID(), location, new Date());

        when(tourGuideService.getUser(isA(String.class))).thenReturn(new User(UUID.randomUUID(), "name", "phone", "mail"));
        when(tourGuideService.getUserLocation(isA(User.class))).thenReturn(visitedLocation);

        String result = tourGuideController.getLocation("test");
        System.out.println(result);
        System.out.println(JsonStream.serialize(visitedLocation));*/
    }

    @Test
    public void goToDisney() {
    }

    @Test
    public void getNearbyAttractions() {
    }

    @Test
    public void getRewards() {
    }

    @Test
    public void getAllCurrentLocations() {
    }

    @Test
    public void getTripDeals() {
    }
}