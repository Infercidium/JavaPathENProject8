package tourGuide;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import tripPricer.TripPricer;

/**
 * Configuration Class.
 */
@Configuration
public class TourGuideModule {

	@Bean
	public TripPricer getTripPricer() {
		return new TripPricer();
	}
}
