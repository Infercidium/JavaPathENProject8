package tourGuide;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Date;
import java.util.List;

import org.junit.Test;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import tourGuide.helper.InternalTestHelper;
import tourGuide.model.Attraction;
import tourGuide.model.VisitedLocation;
import tourGuide.service.RewardsService;
import tourGuide.service.TourGuideService;
import tourGuide.user.User;
import tourGuide.user.UserReward;

@SpringBootTest
public class TestRewardsService {
	@Value("${gpsUtil.url}")
	private String gpsUtilUrlBase = "http://localhost:8080";

	@Test
	public void userGetRewards() {
		RewardsService rewardsService = new RewardsService();

		InternalTestHelper.setInternalUserNumber(1);
		TourGuideService tourGuideService = new TourGuideService(rewardsService);

		User user = tourGuideService.getAllUsers().get(0);

		WebClient gpsClient = WebClient.builder().baseUrl(gpsUtilUrlBase).build();

		Flux<List<Attraction>> attractionFlux = gpsClient.get().uri("/attractions").accept(MediaType.APPLICATION_JSON).retrieve()
				.bodyToFlux(new ParameterizedTypeReference<List<Attraction>>() {});
		List<Attraction> attractionList = attractionFlux.blockLast();
		Attraction attraction = attractionList.get(0);

		user.addToVisitedLocations(new VisitedLocation(user.getUserId(), attraction, new Date()));

		tourGuideService.trackUserLocation(user);

		List<UserReward> userRewards = user.getUserRewards();
		tourGuideService.locationTracker.stopTracking();


		assertEquals(1, userRewards.size());
	}

	@Test
	public void isWithinAttractionProximity() {
		RewardsService rewardsService = new RewardsService();

		WebClient gpsClient = WebClient.builder().baseUrl(gpsUtilUrlBase).build();

		Flux<List<Attraction>> attractionFlux = gpsClient.get().uri("/attractions").accept(MediaType.APPLICATION_JSON).retrieve()
				.bodyToFlux(new ParameterizedTypeReference<List<Attraction>>() {});
		List<Attraction> attractionList = attractionFlux.blockLast();

		Attraction attraction = attractionList.get(0);
		assertTrue(rewardsService.isWithinAttractionProximity(attraction, attraction));
	}

	@Test
	public void nearAllAttractions() {
		RewardsService rewardsService = new RewardsService();
		rewardsService.setProximityBuffer(Integer.MAX_VALUE);

		InternalTestHelper.setInternalUserNumber(1);
		TourGuideService tourGuideService = new TourGuideService(rewardsService);
		tourGuideService.locationTracker.stopTracking();

		WebClient gpsClient = WebClient.builder().baseUrl(gpsUtilUrlBase).build();

		Flux<List<Attraction>> attractionFlux = gpsClient.get().uri("/attractions").accept(MediaType.APPLICATION_JSON).retrieve()
				.bodyToFlux(new ParameterizedTypeReference<List<Attraction>>() {});
		List<Attraction> attractionList = attractionFlux.blockLast();

		tourGuideService.trackUserLocation(tourGuideService.getAllUsers().get(0));

		List<UserReward> userRewards = tourGuideService.getUserRewards(tourGuideService.getAllUsers().get(0));

		assertEquals(attractionList.size(), userRewards.size());
	}
}
