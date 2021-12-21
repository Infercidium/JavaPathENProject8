package tourGuide.controller;

import gpsUtil.GpsUtil;
import gpsUtil.location.Attraction;
import gpsUtil.location.VisitedLocation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
public class GpsUtilController {

    @Autowired
    private GpsUtil gpsUtil;

    @GetMapping("/userLocation/{userID}")
    public VisitedLocation getUserLocation(@PathVariable UUID userID) {
        return gpsUtil.getUserLocation(userID);
    }


    @GetMapping("/attractions")
    public List<Attraction> getAttractions() {
        return gpsUtil.getAttractions();
    }
}
