package tourGuide;

import static org.junit.Assert.assertTrue;

import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.time.StopWatch;
import org.junit.Test;

import org.springframework.boot.test.context.SpringBootTest;
import tourGuide.constant.TrackerParam;
import tourGuide.proxy.GpsUtilProxy;
import tourGuide.helper.InternalTestHelper;
import tourGuide.model.Attraction;
import tourGuide.model.VisitedLocation;
import tourGuide.service.RewardsService;
import tourGuide.service.TourGuideService;
import tourGuide.threads.CalculateRewardsThreads;
import tourGuide.threads.TrackUserLocationThreads;
import tourGuide.user.User;

@SpringBootTest
public class TestPerformance {

	/*
	 * A note on performance improvements:
	 *     
	 *     The number of users generated for the high volume tests can be easily adjusted via this method:
	 *     
	 *     		InternalTestHelper.setInternalUserNumber(100000);
	 *     
	 *     
	 *     These tests can be modified to suit new solutions, just as long as the performance metrics
	 *     at the end of the tests remains consistent. 
	 * 
	 *     These are performance metrics that we are trying to hit:
	 *     
	 *     highVolumeTrackLocation: 100,000 users within 15 minutes:
	 *     		assertTrue(TimeUnit.MINUTES.toSeconds(15) >= TimeUnit.MILLISECONDS.toSeconds(stopWatch.getTime()));
     *
     *     highVolumeGetRewards: 100,000 users within 20 minutes:
	 *          assertTrue(TimeUnit.MINUTES.toSeconds(20) >= TimeUnit.MILLISECONDS.toSeconds(stopWatch.getTime()));
	 */

	@Test
	public void highVolumeTrackLocation() {
		RewardsService rewardsService = new RewardsService();
		// Users should be incremented up to 100,000, and test finishes within 15 minutes
		InternalTestHelper.setInternalUserNumber(100000);
		TourGuideService tourGuideService = new TourGuideService(rewardsService);
		tourGuideService.locationTracker.stopTracking();
		ExecutorService executorLocationService = Executors.newFixedThreadPool(TrackerParam.N_THREADS);

		List<User> allUsers = tourGuideService.getAllUsers();

	    StopWatch stopWatch = new StopWatch();
		stopWatch.start();

		for (User user : allUsers) {
			TrackUserLocationThreads trackUserLocationThreads = new TrackUserLocationThreads(user, tourGuideService);
			executorLocationService.execute(trackUserLocationThreads);
		}

		executorLocationService.shutdown();
		tourGuideService.rewardTracker.stopTracking();
		stopWatch.stop();

		System.out.println("highVolumeTrackLocation: Time Elapsed: " + TimeUnit.MILLISECONDS.toSeconds(stopWatch.getTime()) + " seconds."); 
		assertTrue(TimeUnit.MINUTES.toSeconds(15) >= TimeUnit.MILLISECONDS.toSeconds(stopWatch.getTime()));
	}

	@Test
	public void highVolumeGetRewards() {
		RewardsService rewardsService = new RewardsService();

		// Users should be incremented up to 100,000, and test finishes within 20 minutes
		InternalTestHelper.setInternalUserNumber(100000);
		StopWatch stopWatch = new StopWatch();
		stopWatch.start();
		TourGuideService tourGuideService = new TourGuideService(rewardsService);
		tourGuideService.rewardTracker.stopTracking();
		ExecutorService executorRewardService = Executors.newFixedThreadPool(TrackerParam.N_THREADS);

		GpsUtilProxy gpsUtilProxy = new GpsUtilProxy();

		List<Attraction> attractionList = gpsUtilProxy.attractionsList();

	    Attraction attraction = attractionList.get(0);

		List<User> allUsers = tourGuideService.getAllUsers();
		allUsers.forEach(u -> u.addToVisitedLocations(new VisitedLocation(u.getUserId(), attraction, new Date())));

		for (User user : allUsers) {
			CalculateRewardsThreads calculateRewardsThreads = new CalculateRewardsThreads(user, rewardsService);
			executorRewardService.execute(calculateRewardsThreads);
		}

		executorRewardService.shutdown();
		tourGuideService.locationTracker.stopTracking();
		for(User user : allUsers) {
			assertTrue(user.getUserRewards().size() > 0);
		}

		stopWatch.stop();


		System.out.println("highVolumeGetRewards: Time Elapsed: " + TimeUnit.MILLISECONDS.toSeconds(stopWatch.getTime()) + " seconds."); 
		assertTrue(TimeUnit.MINUTES.toSeconds(20) >= TimeUnit.MILLISECONDS.toSeconds(stopWatch.getTime()));
	}
	
}
