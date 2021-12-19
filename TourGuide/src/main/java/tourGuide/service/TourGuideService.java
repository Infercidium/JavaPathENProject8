package tourGuide.service;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import rewardCentral.RewardCentral;
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
    private final RewardCentral rewardCentral;
    private final TripPricer tripPricer = new TripPricer();
    public final LocationTracker locationTracker;
    public final RewardTracker rewardTracker;
    boolean testMode = true;
    private ExecutorService executorTourGuideService = Executors.newFixedThreadPool(ExecutorThreadParam.N_THREADS);

    @Value("${rewardCentral.url}")
    private String rewardUrlBase;

    @Value("${gpsUtil.url}")
    private String gpsUtilUrlBase = "http://localhost:8080";

    WebClient gpsClient = WebClient.builder().baseUrl(gpsUtilUrlBase).build();

    public TourGuideService(RewardsService rewardsService) {
        this.rewardsService = rewardsService;

        if (testMode) {
            logger.info("TestMode enabled");
            logger.debug("Initializing users");
            initializeInternalUsers();
            logger.debug("Finished initializing users");
        }
        locationTracker = new LocationTracker(this);
        rewardTracker = new RewardTracker(rewardsService, this);
        addShutDownHook();
        rewardCentral = new RewardCentral();
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
        gpsClient.get().uri("/userLocation/{userID}", user.getUserId()).accept(MediaType.APPLICATION_JSON)
                .retrieve().bodyToMono(VisitedLocation.class)
                .subscribe(data -> {user.addToVisitedLocations(data);});
        return user.getLastVisitedLocation();
    }

    public void calculateReward(User user) {
        rewardsService.calculateRewards(user);
        rewardsService.calculateRewardsEnd();
    }

    public UserDto getNearByAttractions(String userName) {
        User user = getUser(userName);
        VisitedLocation visitedLocation = getUserLocation(user);
        UserDto userDto = new UserDto(user);

        Flux<List<Attraction>> attractionFlux = gpsClient.get().uri("/attractions").accept(MediaType.APPLICATION_JSON).retrieve()
                .bodyToFlux(new ParameterizedTypeReference<List<Attraction>>() {});
        List<Attraction> attractionList = attractionFlux.blockLast();
        attractionList.stream().sorted((a1, a2)
                        -> (int) (rewardsService.getDistance(a1, visitedLocation.getLocation()) - rewardsService.getDistance(a2, visitedLocation.getLocation())))
                .collect(Collectors.toList());
        for (int i = 0; i < NearbyAttraction.NEARBY_ATTRACTION_NUMBER; i++) {
            AttractionDto attractionDto = new AttractionDto(attractionList.get(i));
            attractionDto.setDistance(rewardsService.getDistance(visitedLocation.getLocation(), attractionList.get(i)));
            attractionDto.setRewardPoint(rewardCentral.getAttractionRewardPoints(attractionList.get(i).getAttractionId(), user.getUserId()));
            userDto.addAttractionDto(attractionDto);
        }
        return userDto;
    }

    public void GoToDisney(User user) {
        Location location = new Location(33.817595D, -117.922008D);
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
