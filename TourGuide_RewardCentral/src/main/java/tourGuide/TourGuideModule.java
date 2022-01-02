package tourGuide;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import rewardCentral.RewardCentral;

/**
 * Configuration Class.
 */
@Configuration
public class TourGuideModule {
	
	@Bean
	public RewardCentral getRewardCentral() {
		return new RewardCentral();
	}
	
}
