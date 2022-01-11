package unitaire.controller;

import com.jsoniter.output.JsonStream;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;
import tourGuide.controller.TourGuideController;
import tourGuide.dto.UserDto;
import tourGuide.helper.InternalTestHelper;
import tourGuide.model.Attraction;
import tourGuide.model.Location;
import tourGuide.model.Provider;
import tourGuide.model.VisitedLocation;
import tourGuide.service.RewardsService;
import tourGuide.service.TourGuideService;
import tourGuide.user.User;
import tourGuide.user.UserReward;

import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.isA;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest(classes = {TourGuideController.class})
@RunWith(SpringRunner.class)
public class TourGuideControllerTest {

    @MockBean
    private RewardsService rewardsService;

    @MockBean
    private TourGuideService tourGuideService;

    @Autowired
    private TourGuideController tourGuideController;

    private UUID uuid = UUID.randomUUID();
    private User user = new User(uuid, "name", "phone", "mail");
    private Location location = new Location(10, 10);
    private VisitedLocation visitedLocation = new VisitedLocation(uuid, location, new Date());
    private Attraction attraction = new Attraction("attraction", "city", "state", 10, 10);
    private UserReward userReward = new UserReward(visitedLocation, attraction, 100);
    private Provider provider = new Provider(uuid, "name", 100);

    @Before
    public void setUp() {
        InternalTestHelper.setInternalUserNumber(0);
        tourGuideService.resetMap();
        tourGuideService.addUser(user);
        user.addToVisitedLocations(visitedLocation);
        user.addUserReward(userReward);
        when(tourGuideService.getUser(isA(String.class))).thenReturn(user);
        when(tourGuideService.getUserLocation(isA(User.class))).thenReturn(visitedLocation);
    }

    @Test
    public void index() {
        String result = tourGuideController.index();
        assertEquals("Greetings from TourGuide!", result);
    }

    @Test
    public void getLocation() {
        String result = tourGuideController.getLocation("test");
        assertEquals(JsonStream.serialize(visitedLocation.getLocation()), result);
    }

    @Test
    public void goToDisney() {
        String result = tourGuideController.goToDisney("test");
        assertEquals(JsonStream.serialize(visitedLocation.getLocation()), result);
        verify(tourGuideService, times(1)).goToDisney(isA(User.class));
    }

    @Test
    public void getNearbyAttractions() {
        when(tourGuideService.getNearByAttractions("test")).thenReturn(new UserDto(user));
        String result = tourGuideController.getNearbyAttractions("test");
        assertTrue(result.contains(user.getUserName()));
    }

    @Test
    public void getRewards() {
        when(tourGuideService.getUserRewards(isA(User.class))).thenReturn(Collections.singletonList(userReward));
        String result = tourGuideController.getRewards("test");
        assertTrue(result.contains(String.valueOf(userReward.getRewardPoints())));
        assertTrue(result.contains(userReward.attraction.getAttractionName()));
    }

    @Test
    public void getAllCurrentLocations() {
        Map<String, Double> pretest = new HashMap();
        pretest.put("latitude", location.getLatitude());
        pretest.put("longitude", location.getLongitude());
        Map<String, Map<String, Double>> test = new HashMap();
        test.put(user.getUserId().toString(), pretest);
        when(tourGuideService.getAllUsersLocation()).thenReturn(test);
        String result = tourGuideController.getAllCurrentLocations();
        assertEquals(JsonStream.serialize(test), result);
    }

    @Test
    public void getTripDeals() {
        when(tourGuideService.getTripDeals(isA(User.class))).thenReturn(Collections.singletonList(provider));
        String result = tourGuideController.getTripDeals("test");
        assertEquals(JsonStream.serialize(Collections.singletonList(provider)), result);
    }
}