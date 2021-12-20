package tourGuide.service;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import gpsUtil.GpsUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

import org.springframework.web.reactive.function.client.WebClient;
import tourGuide.constant.ExecutorThreadParam;
import tourGuide.constant.NearbyAttraction;
import tourGuide.dto.AttractionDto;
import tourGuide.dto.UserDto;
import tourGuide.helper.InternalTestHelper;
import tourGuide.model.Attraction;
import tourGuide.model.Location;
import tourGuide.model.VisitedLocation;
import tourGuide.tracker.LocationTracker;
import tourGuide.tracker.RewardTracker;
import tourGuide.user.User;
import tourGuide.user.UserReward;
import tripPricer.Provider;
import tripPricer.TripPricer;

@Service
public class TourGuideService {
    private final Logger logger = LoggerFactory.getLogger(TourGuideService.class);
    private final RewardsService rewardsService;
    private final TripPricer tripPricer = new TripPricer();
    public final LocationTracker locationTracker;
    public final RewardTracker rewardTracker;
    boolean testMode = true;
    private ExecutorService executorTourGuideService = Executors.newFixedThreadPool(ExecutorThreadParam.N_THREADS);

    @Value("${gpsUtil.url}")
    private String gpsUtilUrlBase = "http://localhost:8080";

    WebClient gpsClient = WebClient.builder().baseUrl(gpsUtilUrlBase).build();

    @Value("${rewardCentral.url}")
    private String rewardCentralUrlBase = "http://localhost:8080";

    WebClient rewardClient = WebClient.builder().baseUrl(rewardCentralUrlBase).build();

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
        System.out.println("gpsUtil = " + gpsUtilUrlBase);
    }

    public List<UserReward> getUserRewards(User user) {
        return user.getUserRewards();
    }

    public VisitedLocation getUserLocation(User user) {
        return (user.getVisitedLocations().size() > 0) ?
                user.getLastVisitedLocation() :
                null;
        //trackUserLocation(user);
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
        List<Provider> providers = tripPricer.getPrice(tripPricerApiKey, user.getUserId(), user.getUserPreferences().getNumberOfAdults(),
                user.getUserPreferences().getNumberOfChildren(), user.getUserPreferences().getTripDuration(), cumulatativeRewardPoints);
        user.setTripDeals(providers);

        return providers;
    }

    public VisitedLocation trackUserLocation(User user) {
        Locale.setDefault(new Locale("en", "US"));
        GpsUtil gpsUtil = new GpsUtil();

        System.out.println("Bug possible ?"); //TODO VisitedLocation ne peux pas aller dans son equivalent mais fonctionne dans Object, utiliser un mapper ?
        VisitedLocation visitedLocation2 = (VisitedLocation) gpsClient.get().uri("/userLocation/{userID}", user.getUserId()).accept(MediaType.APPLICATION_JSON).retrieve()
                .bodyToMono(Object.class).block();
        System.out.println("Test : " + visitedLocation2);

        gpsUtil.location.VisitedLocation visitedLocation = gpsUtil.getUserLocation(user.getUserId());
        VisitedLocation visitedLocation1 = new VisitedLocation();
        Location location = new Location();
        location.setLatitude(visitedLocation.location.latitude);
        location.setLongitude(visitedLocation.location.longitude);
        visitedLocation1.setLocation(location);
        visitedLocation1.setTimeVisited(visitedLocation.timeVisited);
        visitedLocation1.setUserId(visitedLocation.userId);
        user.addToVisitedLocations(visitedLocation1);
        return user.getLastVisitedLocation();
    }

    public UserDto getNearByAttractions(String userName) {
        User user = getUser(userName);
        VisitedLocation visitedLocation = getUserLocation(user);
        UserDto userDto = new UserDto(user);

        GpsUtil gpsUtil = new GpsUtil();
        //TODO Provisoire
        List<gpsUtil.location.Attraction> attractions = gpsUtil.getAttractions();
        List<Attraction> attractionList = new ArrayList<>();
        for (gpsUtil.location.Attraction attraction : attractions) {
            Attraction attraction1 = new Attraction();
            attraction1.setAttractionId(attraction.attractionId);
            attraction1.setAttractionName(attraction.attractionName);
            attraction1.setCity(attraction.city);
            attraction1.setState(attraction.state);
            attraction1.setLatitude(attraction.latitude);
            attraction1.setLongitude(attraction.longitude);
            attractionList.add(attraction1);
        }

        attractionList.stream().sorted((a1, a2)
                        -> (int) (rewardsService.getDistance(a1, visitedLocation.getLocation()) - rewardsService.getDistance(a2, visitedLocation.getLocation())))
                .collect(Collectors.toList());
        for (int i = 0; i < NearbyAttraction.NEARBY_ATTRACTION_NUMBER; i++) {
            AttractionDto attractionDto = new AttractionDto(attractionList.get(i));
            attractionDto.setDistance(rewardsService.getDistance(visitedLocation.getLocation(), attractionList.get(i)));
            attractionDto.setRewardPoint(rewardClient.get().uri("/RewardCentralPoint/{attractionId}/{userId}", attractionList.get(i).getAttractionId(), user.getUserId())
                    .retrieve().bodyToMono(Integer.class).block());
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
