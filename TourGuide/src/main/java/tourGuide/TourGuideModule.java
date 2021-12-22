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
	public String gpsUtilUrlBase;
	@Bean
	public WebClient gpsClient() {
		return WebClient.create(gpsUtilUrlBase);
	}

	@Value("${rewardCentral.url}")
	public String rewardCentralUrlBase;
	@Bean
	public WebClient rewardClient() {
		return WebClient.create(rewardCentralUrlBase);
	}

	@Value("${tripPricer.url}")
	public String tripPricerUrlBase;
	@Bean
	public WebClient pricerClient() {
		return WebClient.create(tripPricerUrlBase);
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
