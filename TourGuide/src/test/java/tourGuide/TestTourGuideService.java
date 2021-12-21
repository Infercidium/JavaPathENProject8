package tourGuide;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import gpsUtil.GpsUtil;
import org.junit.BeforeClass;
import org.junit.Test;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import tourGuide.dto.UserDto;
import tourGuide.helper.InternalTestHelper;
import tourGuide.model.Provider;
import tourGuide.model.VisitedLocation;
import tourGuide.service.RewardsService;
import tourGuide.service.TourGuideService;
import tourGuide.user.User;

@SpringBootTest
public class TestTourGuideService {

    @BeforeClass
    public static void beforeclass() {

    }


    @Test
    public void getUserLocation() {
        RewardsService rewardsService = new RewardsService();
        InternalTestHelper.setInternalUserNumber(1);
        TourGuideService tourGuideService = new TourGuideService(rewardsService);

        User user = tourGuideService.getAllUsers().get(0);
        VisitedLocation visitedLocation = tourGuideService.trackUserLocation(user);

        tourGuideService.locationTracker.stopTracking();

        assertEquals(user.getUserId(), visitedLocation.getUserId());
    }

    @Test
    public void addUser() {
        RewardsService rewardsService = new RewardsService();
        InternalTestHelper.setInternalUserNumber(0);
        TourGuideService tourGuideService = new TourGuideService(rewardsService);

        User user = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");
        User user2 = new User(UUID.randomUUID(), "jon2", "000", "jon2@tourGuide.com");

        tourGuideService.addUser(user);
        tourGuideService.addUser(user2);

        User retrivedUser = tourGuideService.getUser(user.getUserName());
        User retrivedUser2 = tourGuideService.getUser(user2.getUserName());

        tourGuideService.locationTracker.stopTracking();

        assertEquals(user, retrivedUser);
        assertEquals(user2, retrivedUser2);
    }

    @Test
    public void getAllUsers() {
        RewardsService rewardsService = new RewardsService();
        InternalTestHelper.setInternalUserNumber(0);
        TourGuideService tourGuideService = new TourGuideService(rewardsService);

        User user = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");
        User user2 = new User(UUID.randomUUID(), "jon2", "000", "jon2@tourGuide.com");

        tourGuideService.addUser(user);
        tourGuideService.addUser(user2);

        List<User> allUsers = tourGuideService.getAllUsers();

        tourGuideService.locationTracker.stopTracking();

        assertTrue(allUsers.contains(user));
        assertTrue(allUsers.contains(user2));
    }

    @Test
    public void getAllUsersLocation() {
        RewardsService rewardsService = new RewardsService();
        InternalTestHelper.setInternalUserNumber(5);
        TourGuideService tourGuideService = new TourGuideService(rewardsService);

        Map<String, Map<String, Double>> userLocation = tourGuideService.getAllUsersLocation();

        tourGuideService.locationTracker.stopTracking();

        assertEquals(5, userLocation.size());
    }

    @Test
    public void trackUser() {
        RewardsService rewardsService = new RewardsService();
        InternalTestHelper.setInternalUserNumber(1);
        TourGuideService tourGuideService = new TourGuideService(rewardsService);

        User user = tourGuideService.getAllUsers().get(0);
        VisitedLocation visitedLocation = tourGuideService.trackUserLocation(user);

        tourGuideService.locationTracker.stopTracking();

        assertEquals(user.getUserId(), visitedLocation.getUserId());
    }

    @Test
    public void getNearbyAttractions() {
        RewardsService rewardsService = new RewardsService();
        InternalTestHelper.setInternalUserNumber(1);
        TourGuideService tourGuideService = new TourGuideService(rewardsService);

        UserDto userDto = tourGuideService.getNearByAttractions(tourGuideService.getAllUsers().get(0).getUserName());
        tourGuideService.locationTracker.stopTracking();

        assertEquals(5, userDto.getAttractions().size());
    }

    @Test
    public void getTripDeals() {
        RewardsService rewardsService = new RewardsService();
        InternalTestHelper.setInternalUserNumber(0);
        TourGuideService tourGuideService = new TourGuideService(rewardsService);

        User user = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");

        List<Provider> providers = tourGuideService.getTripDeals(user);

        tourGuideService.locationTracker.stopTracking();

        assertEquals(5, providers.size());
    }
}
