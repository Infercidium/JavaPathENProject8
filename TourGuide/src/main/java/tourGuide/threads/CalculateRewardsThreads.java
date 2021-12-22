package tourGuide.threads;

import tourGuide.service.RewardsService;
import tourGuide.user.User;

public class CalculateRewardsThreads implements Runnable {

    private final User user;
    private final RewardsService rewardsService;

    public CalculateRewardsThreads(User user, RewardsService rewardsService) {
        this.user = user;
        this.rewardsService = rewardsService;
    }

    @Override
    public void run() {
        rewardsService.calculateRewards(user);
    }
}
