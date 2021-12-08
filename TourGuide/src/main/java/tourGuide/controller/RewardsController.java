package tourGuide.controller;

import gpsUtil.location.Attraction;
import gpsUtil.location.Location;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import tourGuide.service.RewardsService;
import tourGuide.user.User;

@RestController
public class RewardsController {

    @Autowired
    RewardsService rewardsService;

    @RequestMapping("/calculateRewards")
    public void calculateRewards(@RequestParam User user) {
        rewardsService.calculateRewards(user);
    }

    @RequestMapping("/calculateRewardsEnd")
    public void calculateRewardsEnd() {
        rewardsService.calculateRewardsEnd();
    }

    @RequestMapping("/getDistance")
    public double getDistance(@RequestParam Attraction attraction, @RequestParam Location Location) {
        return rewardsService.getDistance(attraction, Location);
    }
}
