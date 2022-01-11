package unitaire.service;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;
import tourGuide.model.Attraction;
import tourGuide.model.Location;
import tourGuide.model.VisitedLocation;
import tourGuide.proxy.GpsUtilProxy;
import tourGuide.proxy.RewardCentralProxy;
import tourGuide.service.RewardsService;
import tourGuide.user.User;

import java.util.Collections;
import java.util.Date;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

@SpringBootTest(classes = {RewardsService.class})
@RunWith(SpringRunner.class)
public class RewardsServiceTest {

    @MockBean
    private GpsUtilProxy gpsUtilProxy;

    @MockBean
    private RewardCentralProxy rewardCentralProxy;

    @Autowired
    private RewardsService rewardsService;

    private UUID uuid = UUID.randomUUID();
    private User user = new User(uuid, "name", "phone", "mail");
    private Attraction attraction = new Attraction("attraction", "city", "state", 10, 10);
    private Location location1 = new Location(10, 10);
    private Location location2 = new Location(11, 9);
    private VisitedLocation visitedLocation = new VisitedLocation(uuid, location1, new Date());

    @Test
    public void calculateRewards() {
        user.addToVisitedLocations(visitedLocation);
        when(gpsUtilProxy.attractionsList()).thenReturn(Collections.singletonList(attraction));
        assertEquals(0, user.getUserRewards().size());

        rewardsService.calculateRewards(user);
        assertEquals(1, user.getUserRewards().size());
    }

    @Test
    public void getDistance() {
        double result = rewardsService.getDistance(location1, location2);
        Double distance = 96.9789273284309;
        assertTrue(distance.equals(result));
    }
}