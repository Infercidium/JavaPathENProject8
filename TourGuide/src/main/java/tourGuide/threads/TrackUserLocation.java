package tourGuide.threads;

import gpsUtil.GpsUtil;
import gpsUtil.location.VisitedLocation;
import tourGuide.user.User;

import java.util.Locale;

public class TrackUserLocation implements Runnable {
    private final GpsUtil gpsUtil;
    private final User user;

    public TrackUserLocation(GpsUtil gpsUtil, User user) {
        this.gpsUtil = gpsUtil;
        this.user = user;
    }

    @Override
    public void run() {
        Locale.setDefault(new Locale("en", "US"));
        VisitedLocation visitedLocation = gpsUtil.getUserLocation(user.getUserId());
        user.addToVisitedLocations(visitedLocation);
    }
}
