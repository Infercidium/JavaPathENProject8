package tourGuide.service;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import tourGuide.constant.NearbyAttraction;
import tourGuide.dto.AttractionDto;
import tourGuide.dto.UserDto;
import tourGuide.proxy.GpsUtilProxy;
import tourGuide.proxy.RewardCentralProxy;
import tourGuide.proxy.TripPricerProxy;
import tourGuide.helper.InternalTestHelper;
import tourGuide.model.Attraction;
import tourGuide.model.Location;
import tourGuide.model.Provider;
import tourGuide.model.VisitedLocation;
import tourGuide.tracker.LocationTracker;
import tourGuide.tracker.RewardTracker;
import tourGuide.user.User;
import tourGuide.user.UserReward;

@Service
public class TourGuideService {
    private final Logger logger = LoggerFactory.getLogger(TourGuideService.class);
    private final RewardsService rewardsService;
    public final LocationTracker locationTracker;
    public final RewardTracker rewardTracker;
    boolean testMode = true;

    RewardCentralProxy rewardCentralProxy = new RewardCentralProxy();
    GpsUtilProxy gpsUtilProxy = new GpsUtilProxy();
    TripPricerProxy tripPricerProxy = new TripPricerProxy();

    public TourGuideService(RewardsService rewardsService) {
        this.rewardsService = rewardsService;

        if (testMode) {
            logger.info("TestMode enabled");
            logger.debug("Initializing users");
            initializeInternalUsers();
            logger.debug("Finished initializing users");
        }
        locationTracker = new LocationTracker(this);
        rewardTracker = new RewardTracker(this.rewardsService, this);
        addShutDownHook();
    }

    public List<UserReward> getUserRewards(User user) {
        return user.getUserRewards();
    }

    public VisitedLocation getUserLocation(User user) {
        return (user.getVisitedLocations().size() > 0) ?
                user.getLastVisitedLocation() :
                null;
    }

    public User getUser(String userName) {
        return internalUserMap.get(userName);
    }

    public List<User> getAllUsers() {
        return internalUserMap.values().stream().collect(Collectors.toList());
    }

    public Map<String, Map<String, Double>> getAllUsersLocation() {
        Map<String, Map<String, Double>> usersLocation = new HashMap<>();
        for (User currentUser : getAllUsers()) {
            Map<String, Double> currentLocation = new HashMap<>();
            currentLocation.put("longitude", currentUser.getLastVisitedLocation().getLocation().getLongitude());
            currentLocation.put("latitude", currentUser.getLastVisitedLocation().getLocation().getLatitude());
            usersLocation.put(currentUser.getUserId().toString(), currentLocation);
        }
        return usersLocation;
    }

    public void addUser(User user) {
        if (!internalUserMap.containsKey(user.getUserName())) {
            internalUserMap.put(user.getUserName(), user);
        }
    }

    public List<Provider> getTripDeals(User user) {
        int cumulatativeRewardPoints = user.getUserRewards().stream().mapToInt(i -> i.getRewardPoints()).sum();
        List<Provider> providers = tripPricerProxy.price(tripPricerApiKey, user, cumulatativeRewardPoints);
        user.setTripDeals(providers);

        return providers;
    }

    public void trackUserLocation(User user) {
        synchronized (user) {
            VisitedLocation visitedLocation = gpsUtilProxy.visitedLocation(user);
            user.addToVisitedLocations(visitedLocation);
        }
    }

    public UserDto getNearByAttractions(String userName) {
        User user = getUser(userName);
        VisitedLocation visitedLocation = getUserLocation(user);
        UserDto userDto = new UserDto(user);

        List<Attraction> attractionList = gpsUtilProxy.attractionsList().stream().sorted((a1, a2) -> (int)
                (rewardsService.getDistance(a1, visitedLocation.getLocation()) - rewardsService.getDistance(a2, visitedLocation.getLocation()))).collect(Collectors.toList());

        for (int i = 0; i < NearbyAttraction.NEARBY_ATTRACTION_NUMBER; i++) {
            AttractionDto attractionDto = new AttractionDto(attractionList.get(i));
            attractionDto.setDistance(rewardsService.getDistance(visitedLocation.getLocation(), attractionList.get(i)));
            attractionDto.setRewardPoint(rewardCentralProxy.rewardPoint(attractionList.get(i), user));
            userDto.addAttractionDto(attractionDto);
        }
        return userDto;
    }

    public void GoToDisney(User user) {
        Location location = new Location( -117.922008D, 33.817595D);
        VisitedLocation visitedLocation = new VisitedLocation(user.getUserId(), location, new Date());
        user.addToVisitedLocations(visitedLocation);
    }

    private void addShutDownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                locationTracker.stopTracking();
            }
        });
        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                rewardTracker.stopTracking();
            }
        });
    }

    /**********************************************************************************
     *
     * Methods Below: For Internal Testing
     *
     **********************************************************************************/
    private static final String tripPricerApiKey = "test-server-api-key";
    // Database connection will be used for external users, but for testing purposes internal users are provided and stored in memory
    private final Map<String, User> internalUserMap = new HashMap<>();

    private void initializeInternalUsers() {
        IntStream.range(0, InternalTestHelper.getInternalUserNumber()).forEach(i -> {
            String userName = "internalUser" + i;
            String phone = "000";
            String email = userName + "@tourGuide.com";
            User user = new User(UUID.randomUUID(), userName, phone, email);
            generateUserLocationHistory(user);

            internalUserMap.put(userName, user);
        });
        logger.debug("Created " + InternalTestHelper.getInternalUserNumber() + " internal test users.");
    }

    private void generateUserLocationHistory(User user) {
        IntStream.range(0, 3).forEach(i -> {
            user.addToVisitedLocations(new VisitedLocation(user.getUserId(), new Location(generateRandomLatitude(), generateRandomLongitude()), getRandomTime()));
        });
    }

    private double generateRandomLongitude() {
        double leftLimit = -180;
        double rightLimit = 180;
        return leftLimit + new Random().nextDouble() * (rightLimit - leftLimit);
    }

    private double generateRandomLatitude() {
        double leftLimit = -85.05112878;
        double rightLimit = 85.05112878;
        return leftLimit + new Random().nextDouble() * (rightLimit - leftLimit);
    }

    private Date getRandomTime() {
        LocalDateTime localDateTime = LocalDateTime.now().minusDays(new Random().nextInt(30));
        return Date.from(localDateTime.toInstant(ZoneOffset.UTC));
    }
}
