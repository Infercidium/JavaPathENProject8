# TourGuide
The TourGuide application allows via an internet connection to have information on tourist attractions, to propose the closest attractions according to user preferences.
But also to accumulate points by visiting attractions in order to reuse them with our partner travel agencies.

## Composing

### APPLICATION
The application is composed of Spring boot + Docker / Docker Compose + Gradle.

1. `/`
  * No parameters required.
  * Displays a welcome message in the app.

2. `/getLocation`
  * Username of the user is required.
  * Displays the user's coordinates (latitude and longitude).
  
3. `/goToDisneyland`
  * Username of the user is required. Used in the context of testing.
  * Moves the user's coordinates to the Disneyland attraction's coordinates and displays the new coordinates.
 
4. `/getNearbyAttractions`
  * Username of the user is required.
  * Displays the 5 attractions closest to the user in order of distance, 
    with the name of the attraction, the distance from the user, its position (latitude and longitude) and the number of reward points if she is visited.
  
5. `/getRewards`
  * Username of the user is required.
  * Displays the user's rewards.

6. `/getAllCurrentLocations`
  * No parameters required.
  * Displays the UUID and correlations (latitude and longitude) of all users.
  
  7. `/getTripDeals`
  * Username of the user is required.
  * Displays 5 travel agency offers according to the preferences and the number of points of the user.
  
## Launch
Application uses docker-compose, use the following command to start it:
`docker-compose start` and stop with `docker-compose stop`

### First Launch
During the first launch, it is important to check if the `ports` used by TourGuide are not already occupied on your machine, 
this information is available in `docker-compose`: `tourguide_net`, the `ipv4_adress` of each container and the `networks`.

Setup for docker-compose:

Entry into the Main module: 
`cd TourGuide`

BootJar construction:
`./gradlew bootjar`

Back to the whole project:
`cd..`

Entry into the GpsUtil module:
`cd TourGuide_GpsUtil`

BootJar construction:
`./gradlew bootjar`

Back to the whole project:
`cd..`

Entry into the RewardCentral module:
`cd TourGuide_RewardCentral`

BootJar construction:
`./gradlew bootjar`

Back to the whole project:
`cd..`

Entering the TripPricer module:
`cd TourGuide_TripPricer`

BootJar construction:
`./gradlew bootjar`

Back to the whole project:
`cd..`

And finally `docker-compose up`, for stop use `docker-compose stop`.

## Testing
This app has Unit test written. Once the test has been generated, it can be accessed by following this path: 

`JavaPathENProject8\TourGuide\build\jacocoHtml\index.html` for the Main module, 

`JavaPathENProject8\TourGuide_GpsUtil\build\jacocoHtml\index.html` for the GpsUtil module, 

`JavaPathENProject8\TourGuide_RewardCentral\build\jacocoHtml\index.html` for the RewardCentral module 

and `JavaPathENProject8\TourGuide_TripPricer\build\jacocoHtml\index.html` for the TripPricer module.

### Test Report

To generate the 4 reports:

Entry into the Main module: 
`cd TourGuide`

Test generation:
`./gradlew test`

Back to the whole project:
`cd..`

Entry into the GpsUtil module:
`cd TourGuide_GpsUtil`

Test generation:
`./gradlew test`

Back to the whole project:
`cd..`

Entry into the RewardCentral module:
`cd TourGuide_RewardCentral`

Test generation:
`./gradlew test`

Back to the whole project:
`cd..`

Entering the TripPricer module:
`cd TourGuide_TripPricer`

Test generation:
`./gradlew test`

Back to the whole project:
`cd..`
