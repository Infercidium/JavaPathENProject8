package tourGuide;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import gpsUtil.GpsUtil;

/**
 * Configuration Class.
 */
@Configuration
public class TourGuideModule {

	@Bean
	public GpsUtil getGpsUtil() {
		return new GpsUtil();
	}
}
