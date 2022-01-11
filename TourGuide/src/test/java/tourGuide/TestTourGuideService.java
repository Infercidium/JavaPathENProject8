package tourGuide;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.junit.Test;

import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import tourGuide.dto.UserDto;
import tourGuide.helper.InternalTestHelper;
import tourGuide.model.Provider;
import tourGuide.model.VisitedLocation;
import tourGuide.service.RewardsService;
import tourGuide.service.TourGuideService;
import tourGuide.user.User;

@SpringBootTest
@RunWith(SpringRunner.class)
public class TestTourGuideService {

    @Autowired
    private RewardsService rewardsService;

    @Autowired
    private TourGuideService tourGuideService;

    @Test
    public void getUserLocation() {
        InternalTestHelper.setInternalUserNumber(1);
        tourGuideService.resetMap();

        User user = tourGuideService.getAllUsers().get(0);
        VisitedLocation visitedLocation = user.getLastVisitedLocation();

        tourGuideService.locationTracker.stopTracking();
        tourGuideService.rewardTracker.stopTracking();

        assertEquals(user.getUserId(), visitedLocation.getUserId());
    }

    @Test
    public void addUser() {
        InternalTestHelper.setInternalUserNumber(0);
        tourGuideService.resetMap();

        User user = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");
        User user2 = new User(UUID.randomUUID(), "jon2", "000", "jon2@tourGuide.com");

        tourGuideService.addUser(user);
        tourGuideService.addUser(user2);

        User retrivedUser = tourGuideService.getUser(user.getUserName());
        User retrivedUser2 = tourGuideService.getUser(user2.getUserName());

        tourGuideService.locationTracker.stopTracking();
        tourGuideService.rewardTracker.stopTracking();

        assertEquals(user, retrivedUser);
        assertEquals(user2, retrivedUser2);
    }

    @Test
    public void getAllUsers() {
        InternalTestHelper.setInternalUserNumber(0);
        tourGuideService.resetMap();

        User userall1 = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");
        User userall2 = new User(UUID.randomUUID(), "jon2", "000", "jon2@tourGuide.com");

        tourGuideService.addUser(userall1);
        tourGuideService.addUser(userall2);

        List<User> allUsers = tourGuideService.getAllUsers();

        tourGuideService.locationTracker.stopTracking();
        tourGuideService.rewardTracker.stopTracking();

        assertTrue(allUsers.contains(userall1));
        assertTrue(allUsers.contains(userall2));
    }

    @Test
    public void getAllUsersLocation() {
        InternalTestHelper.setInternalUserNumber(5);
        tourGuideService.resetMap();

        Map<String, Map<String, Double>> userLocation = tourGuideService.getAllUsersLocation();

        tourGuideService.locationTracker.stopTracking();
        tourGuideService.rewardTracker.stopTracking();

        assertEquals(5, userLocation.size());
    }

    @Test
    public void trackUser() {
        InternalTestHelper.setInternalUserNumber(1);
        tourGuideService.resetMap();

        User user = tourGuideService.getAllUsers().get(0);
        VisitedLocation visitedLocation = user.getLastVisitedLocation();

        tourGuideService.locationTracker.stopTracking();
        tourGuideService.rewardTracker.stopTracking();

        assertEquals(user.getUserId(), visitedLocation.getUserId());
    }

    @Test
    public void getNearbyAttractions() {
        InternalTestHelper.setInternalUserNumber(1);
        tourGuideService.resetMap();

        UserDto userDto = tourGuideService.getNearByAttractions(tourGuideService.getAllUsers().get(0).getUserName());

        tourGuideService.locationTracker.stopTracking();
        tourGuideService.rewardTracker.stopTracking();

        assertEquals(5, userDto.getAttractions().size());
    }

    @Test
    public void getTripDeals() {
        InternalTestHelper.setInternalUserNumber(0);

        User user = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");

        List<Provider> providers = tourGuideService.getTripDeals(user);

        tourGuideService.locationTracker.stopTracking();
        tourGuideService.rewardTracker.stopTracking();

        assertEquals(5, providers.size());
    }
}
