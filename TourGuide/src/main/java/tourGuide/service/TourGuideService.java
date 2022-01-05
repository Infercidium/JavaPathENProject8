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
import org.springframework.beans.factory.annotation.Autowired;
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

    @Autowired
    private RewardCentralProxy rewardCentralProxy;

    @Autowired
    private GpsUtilProxy gpsUtilProxy;

    @Autowired
    private TripPricerProxy tripPricerProxy;

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

    /**
     * Provides the reward list of the selected user.
     * @param user selected.
     * @return list of reward.
     */
    public List<UserReward> getUserRewards(User user) {
        return user.getUserRewards();
    }

    /**
     * Provides the location of the selected user.
     * @param user selected.
     * @return location.
     */
    public VisitedLocation getUserLocation(User user) {
        return (user.getVisitedLocations().size() > 0) ?
                user.getLastVisitedLocation() :
                null;
    }

    /**
     * Retrieves the user corresponding to the username.
     * @param userName of user.
     * @return user.
     */
    public User getUser(String userName) {
        return internalUserMap.get(userName);
    }

    /**
     * Retrieves the list of internalUsermap users.
     * @return list of user.
     */
    public List<User> getAllUsers() {
        return internalUserMap.values().stream().collect(Collectors.toList());
    }

    /**
     * Makes a map containing the list of users and their coordinates.
     * @return map.
     */
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

    /**
     * Add a user to the list.
     * @param user to add.
     */
    public void addUser(User user) {
        if (!internalUserMap.containsKey(user.getUserName())) {
            internalUserMap.put(user.getUserName(), user);
        }
    }

    /**
     * Reset internalUserMap for test.
     */
    public void resetMap() {
        if (internalUserMap.size() > 0) {
            internalUserMap.clear();
        }
        initializeInternalUsers();
    }

    /**
     * Call on tripPricer to get 5 TripDeal based on reward points.
     * @param user : reward Point
     * @return the list of Provider obtained.
     */
    public List<Provider> getTripDeals(User user) {
        int cumulatativeRewardPoints = user.getUserRewards().stream().mapToInt(i -> i.getRewardPoints()).sum();
        List<Provider> providers = tripPricerProxy.price(tripPricerApiKey, user, cumulatativeRewardPoints);
        user.setTripDeals(providers);

        return providers;
    }

    /**
     * Get the user's position and add it, the user is synchronized so that the two methods do not interfere.
     * @param user to verify.
     */
    public void trackUserLocation(User user) {
        synchronized (user) {
            VisitedLocation visitedLocation = gpsUtilProxy.visitedLocation(user);
            user.addToVisitedLocations(visitedLocation);
        }
    }

    /**
     * Create a dto containing the 5 attractions closest to the user. (the 5 can be modified in the constants : NEARBY_ATTRACTION_NUMBER).
     * @param userName of the user.
     * @return a dto containing the 5 attractions closest to the user.
     */
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

    /**
     * Move the selected user to the Disneyland coordinate, use during test.
     * @param user selected.
     */
    public void GoToDisney(User user) {
        Location location = new Location( -117.922008D, 33.817595D);
        VisitedLocation visitedLocation = new VisitedLocation(user.getUserId(), location, new Date());
        user.addToVisitedLocations(visitedLocation);
    }

    /**
     * Turn off Trackers when closing the application.
     */
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
