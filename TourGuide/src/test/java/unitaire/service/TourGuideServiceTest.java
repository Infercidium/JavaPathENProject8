package unitaire.service;

import com.jsoniter.output.JsonStream;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;
import tourGuide.dto.UserDto;
import tourGuide.helper.InternalTestHelper;
import tourGuide.model.Attraction;
import tourGuide.model.Location;
import tourGuide.model.Provider;
import tourGuide.model.VisitedLocation;
import tourGuide.proxy.GpsUtilProxy;
import tourGuide.proxy.RewardCentralProxy;
import tourGuide.proxy.TripPricerProxy;
import tourGuide.service.RewardsService;
import tourGuide.service.TourGuideService;
import tourGuide.user.User;
import tourGuide.user.UserReward;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.isA;
import static org.mockito.Mockito.when;

@SpringBootTest(classes = {TourGuideService.class})
@RunWith(SpringRunner.class)
public class TourGuideServiceTest {

    @MockBean
    private RewardsService rewardsService;

    @MockBean
    private TripPricerProxy tripPricerProxy;

    @MockBean
    private GpsUtilProxy gpsUtilProxy;

    @MockBean
    private RewardCentralProxy rewardCentralProxy;

    @Autowired
    private TourGuideService tourGuideService;


    private UUID uuid = UUID.randomUUID();
    private UUID uuid2 = UUID.randomUUID();
    private User user = new User(uuid, "name", "phone", "mail");
    private Location location = new Location(10, 10);
    private Location disneyLocation = new Location( -117.922008D, 33.817595D);
    private VisitedLocation visitedLocation = new VisitedLocation(uuid, location, new Date());
    private VisitedLocation visitedLocation2 = new VisitedLocation(uuid2, location, new Date());
    private Attraction attraction = new Attraction("attraction", "city", "state", 10, 10);
    private UserReward userReward = new UserReward(visitedLocation, attraction, 100);
    private Provider provider = new Provider(uuid, "name", 100);

    @Before
    public void setUp() throws Exception {
        InternalTestHelper.setInternalUserNumber(0);
        tourGuideService.resetMap();
        tourGuideService.addUser(user);
        user.addToVisitedLocations(visitedLocation);
        user.addUserReward(userReward);
    }

    @Test
    public void getUserRewards() {
        UserReward result = tourGuideService.getUserRewards(user).get(0);
        assertEquals(userReward, result);
    }

    @Test
    public void getUserLocation() {
        VisitedLocation result = tourGuideService.getUserLocation(user);
        assertEquals(visitedLocation, result);
    }

    @Test
    public void getUser() {
        User result = tourGuideService.getUser(user.getUserName());
        assertEquals(user, result);
    }

    @Test
    public void getAllUsers() {
        List<User> result = tourGuideService.getAllUsers();
        assertEquals(Collections.singletonList(user), result);
    }

    @Test
    public void getAllUsersLocation() {
        Map<String, Map<String, Double>> result = tourGuideService.getAllUsersLocation();
        assertEquals("{" + uuid + "={latitude=10.0, longitude=10.0}}", result.toString());
    }

    @Test
    public void addUser() {
        User user2 = new User(uuid2, "name2", "phone2", "mail2");
        tourGuideService.addUser(user2);
        assertTrue(tourGuideService.getAllUsers().contains(user2));
    }

    @Test
    public void resetMap() {
        InternalTestHelper.setInternalUserNumber(10);
        tourGuideService.resetMap();
        assertEquals(10, tourGuideService.getAllUsers().size());
    }

    @Test
    public void getTripDeals() {
        when(tripPricerProxy.price(isA(String.class), isA(User.class), isA(Integer.class))).thenReturn(Collections.singletonList(provider));

        Provider result = tourGuideService.getTripDeals(user).get(0);
        assertEquals(provider, result);
    }

    @Test
    public void trackUserLocation() {
        when(gpsUtilProxy.visitedLocation(isA(User.class))).thenReturn(visitedLocation2);

        tourGuideService.trackUserLocation(user);
        assertEquals(uuid2, user.getLastVisitedLocation().getUserId());
    }

    @Test
    public void getNearByAttractions() {
        List<Attraction> test = new ArrayList<>();
        test.add(attraction);
        test.add(new Attraction("attraction2", "city2", "state2", 12, 12));
        test.add(new Attraction("attraction3", "city3", "state3", 13, 13));
        test.add(new Attraction("attraction4", "city4", "state4", 14, 14));
        test.add(new Attraction("attraction5", "city5", "state5", 15, 15));
        when(gpsUtilProxy.attractionsList()).thenReturn(test);
        UserDto result = tourGuideService.getNearByAttractions("name");
        assertEquals("attraction", result.getAttractions().get(0).getName());
        assertEquals("attraction5", result.getAttractions().get(4).getName());
        assertEquals(5, result.getAttractions().size());
    }

    @Test
    public void goToDisney() {
        tourGuideService.goToDisney(user);
        assertEquals(JsonStream.serialize(disneyLocation), JsonStream.serialize(user.getLastVisitedLocation().getLocation()));
    }
}