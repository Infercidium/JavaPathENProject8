package tourGuide.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import tripPricer.Provider;
import tripPricer.TripPricer;

import java.util.List;
import java.util.UUID;

@RestController
public class TripPricerController {

    @Autowired
    private TripPricer tripPricer;

    @GetMapping("/price/{apiKey}/{attractionId}/{adult}/{children}/}{nightsStay}/{rewardsPoints}")
    public List<Provider> getPricer(@PathVariable String apiKey, @PathVariable UUID attractionId, @PathVariable int adult,
                                    @PathVariable int children, @PathVariable int nightsStay, @PathVariable int rewardsPoints) {
        return tripPricer.getPrice(apiKey, attractionId, adult, children, nightsStay, rewardsPoints);
    }
}
