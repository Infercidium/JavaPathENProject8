package tourGuide;

import static org.junit.Assert.assertEquals;

import java.util.Date;
import java.util.List;

import org.junit.Test;

import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import tourGuide.proxy.GpsUtilProxy;
import tourGuide.helper.InternalTestHelper;
import tourGuide.model.Attraction;
import tourGuide.model.VisitedLocation;
import tourGuide.service.RewardsService;
import tourGuide.service.TourGuideService;
import tourGuide.user.User;
import tourGuide.user.UserReward;

@SpringBootTest
@RunWith(SpringRunner.class)
public class TestRewardsService {

	@Autowired
	RewardsService rewardsService;

	@Autowired
	TourGuideService tourGuideService;

	@Autowired
	GpsUtilProxy gpsUtilProxy;

	@Test
	public void userGetRewards() {
		InternalTestHelper.setInternalUserNumber(1);
		tourGuideService.resetMap();

		User user = tourGuideService.getAllUsers().get(0);

		List<Attraction> attractionList = gpsUtilProxy.attractionsList();
		Attraction attraction = attractionList.get(0);

		user.addToVisitedLocations(new VisitedLocation(user.getUserId(), attraction, new Date()));

		tourGuideService.trackUserLocation(user);
		rewardsService.calculateRewards(user);

		List<UserReward> userRewards = user.getUserRewards();
		tourGuideService.locationTracker.stopTracking();
		tourGuideService.rewardTracker.stopTracking();

		assertEquals(1, userRewards.size());
	}

	@Test
	public void nearAllAttractions() {
		rewardsService.setProximityBuffer(Integer.MAX_VALUE);
		InternalTestHelper.setInternalUserNumber(1);
		tourGuideService.resetMap();

		tourGuideService.locationTracker.stopTracking();
		tourGuideService.rewardTracker.stopTracking();

		List<Attraction> attractionList = gpsUtilProxy.attractionsList();

		tourGuideService.trackUserLocation(tourGuideService.getAllUsers().get(0));
		rewardsService.calculateRewards(tourGuideService.getAllUsers().get(0));

		List<UserReward> userRewards = tourGuideService.getUserRewards(tourGuideService.getAllUsers().get(0));

		rewardsService.setProximityBuffer(10);
		assertEquals(attractionList.size(), userRewards.size());
	}
}
