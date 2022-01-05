package tourGuide;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.web.reactive.function.client.WebClient;
import tourGuide.service.RewardsService;

/**
 * Configuration Class.
 */
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
}
