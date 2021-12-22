package tourGuide;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import gpsUtil.GpsUtil;
import org.springframework.web.reactive.function.client.WebClient;
import rewardCentral.RewardCentral;
import tourGuide.service.RewardsService;
import tripPricer.TripPricer;

@Configuration
public class TourGuideModule {
	@Value("${gpsUtil.url}")
	private String gpsUtilUrlBase;

	@Bean
	WebClient gpsClient() {
		return WebClient.builder().baseUrl(gpsUtilUrlBase).build();
	}

	@Value("${rewardCentral.url}")
	private String rewardCentralUrlBase;

	@Bean
	WebClient rewardClient() {
		return WebClient.builder().baseUrl(rewardCentralUrlBase).build();
	}

	@Value("${tripPricer.url}")
	private String tripPricerUrlBase;

	@Bean
	WebClient pricerClient() {
		return WebClient.builder().baseUrl(tripPricerUrlBase).build();
	}

	@Bean
	public GpsUtil getGpsUtil() {
		return new GpsUtil();
	}

	@Bean
	public TripPricer getTripPricer() {
		return new TripPricer();
	}
	
	@Bean
	public RewardsService getRewardsService() {
		return new RewardsService();
	}
	
	@Bean
	public RewardCentral getRewardCentral() {
		return new RewardCentral();
	}
	
}
