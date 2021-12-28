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

    /**
     * Starts a new Thread executing the calculateRewards method with the "User" parameter.
     */
    @Override
    public void run() {
        rewardsService.calculateRewards(user);
    }
}
