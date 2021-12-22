package tourGuide.threads;

import tourGuide.service.TourGuideService;
import tourGuide.user.User;

public class TrackUserLocationThreads implements Runnable {

    private final User user;
    private final TourGuideService tourGuideService;

    public TrackUserLocationThreads(User user, TourGuideService tourGuideService) {
        this.user = user;
        this.tourGuideService = tourGuideService;
    }

    @Override
    public void run() {
        tourGuideService.trackUserLocation(user);
    }
}
