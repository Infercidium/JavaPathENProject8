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

`cd TourGuide`

`./gradlew bootjar`

`cd..`

`cd TourGuide_GpsUtil`

`./gradlew bootjar`

`cd..`

`cd TourGuide_RewardCentral`

`./gradlew bootjar`

`cd..`

`cd TourGuide_TripPricer`

`./gradlew bootjar`

`cd..`

And finally `docker-compose up`, for stop use `docker-compose stop`.

## Testing A FAIRE
This application has Unit tests written.
It is possible to have access to Surefire Report and Jacoco to visualize the execution time and the coverage of the tests following this path:
`PayMyBuddy/target/site/project-reports.html`

### Test Report

After using the following command in the terminal:
`mvn clean verify site`

### Checkstyle Report

Checkstyle results takes into account the classes generated automatically by the Mapper, adding more than 300 uncorrectable errors.
But it is possible to make them disappear with the command:
`mvn site`

However after that it is possible that the application is having difficulties working correctly, in this case you have to do:
`mvn clean`
