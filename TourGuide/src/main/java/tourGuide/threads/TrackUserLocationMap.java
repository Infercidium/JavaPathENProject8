package tourGuide.threads;

import gpsUtil.GpsUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tourGuide.service.RewardsService;
import tourGuide.service.TourGuideService;

@Configuration
public class TrackUserLocationMap {

    private static final Logger LOGGER = LoggerFactory.getLogger(TrackUserLocationMap.class);

    private final GpsUtil gpsUtil;
    private final RewardsService rewardsService;

    public TrackUserLocationMap(GpsUtil gpsUtil, RewardsService rewardsService) {
        this.gpsUtil = gpsUtil;
        this.rewardsService = rewardsService;
    }

    @Bean
    public CommandLineRunner beginMap() {
        return args -> {
            TourGuideService tourGuideService = new TourGuideService(gpsUtil, rewardsService);
            Thread thread = new Thread(tourGuideService);
            thread.start();
            LOGGER.info("Mapping Location = ON");
        };
    }
}
