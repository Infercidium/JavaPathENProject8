package tourGuide.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import rewardCentral.RewardCentral;

import java.util.UUID;

@RestController
public class RewardCentralController {

    @Autowired
    private RewardCentral rewardCentral;

    @RequestMapping("/RewardCentralPoint/{attractionId}/{userId}")
    public int getAttractionRewardPoints(@PathVariable UUID attractionId, @PathVariable UUID userId) {
        return rewardCentral.getAttractionRewardPoints(attractionId, userId);
    }
}
