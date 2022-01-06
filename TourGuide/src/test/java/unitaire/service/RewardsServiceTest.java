package unitaire.service;

import org.junit.Test;
import org.springframework.boot.test.context.SpringBootTest;
import tourGuide.model.Location;
import tourGuide.service.RewardsService;

import static org.junit.Assert.assertTrue;

@SpringBootTest(classes = {RewardsService.class})
public class RewardsServiceTest {

    RewardsService rewardsService = new RewardsService();

    Location location1 = new Location(10, 10);
    Location location2 = new Location(11, 9);

    @Test
    public void calculateRewards() { //TODO Blocage
    }

    @Test
    public void getDistance() {
        double result = rewardsService.getDistance(location1, location2);
        Double distance = 96.9789273284309;
        assertTrue(distance.equals(result));
    }
}