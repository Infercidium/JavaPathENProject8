package tourGuide.controller;

import com.jsoniter.output.JsonStream;
import gpsUtil.GpsUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
public class GpsUtilController {

    @Autowired
    private GpsUtil gpsUtil;

    @GetMapping("/userLocation/{userID}")
    public String getUserLocation(@PathVariable UUID userID) {
        return JsonStream.serialize(gpsUtil.getUserLocation(userID));
    }


    @GetMapping("/attractions")
    public String getAttractions() {
        return JsonStream.serialize(gpsUtil.getAttractions());
    }
}
