package tourGuide.tracker;

import org.apache.commons.lang3.time.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tourGuide.constant.TrackerParam;
import tourGuide.service.RewardsService;
import tourGuide.service.TourGuideService;
import tourGuide.threads.CalculateRewardsThreads;
import tourGuide.user.User;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class RewardTracker extends Thread {
    private final Logger LOGGER = LoggerFactory.getLogger(RewardTracker.class);
    private static final long trackingPollingInterval = TimeUnit.MINUTES.toSeconds(TrackerParam.WAITING_MINUTE);
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private final ExecutorService executorRewardService = Executors.newFixedThreadPool(TrackerParam.N_THREADS);

    private final TourGuideService tourGuideService;
    private final RewardsService rewardsService;
    private boolean stop = false;

    public RewardTracker(RewardsService rewardsService, TourGuideService tourGuideService) {
        this.rewardsService = rewardsService;
        this.tourGuideService = tourGuideService;

        executorService.submit(this);
    }

    /**
     * Assures to shut down the Tracker thread
     */
    public void stopTracking() {
        stop = true;
        executorService.shutdownNow();
    }

    @Override
    public void run() {
        StopWatch stopWatch = new StopWatch();
        try {
            Thread.sleep(2500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        while(true) {
            if(Thread.currentThread().isInterrupted() || stop) {
                LOGGER.debug("Tracker stopping");
                break;
            }

            List<User> users = tourGuideService.getAllUsers();
            LOGGER.debug("Begin Tracker. Tracking " + users.size() + " users.");
            stopWatch.start();

            for (User user : users) {
                CalculateRewardsThreads calculateRewardsThreads = new CalculateRewardsThreads(user, this.rewardsService);
                executorRewardService.execute(calculateRewardsThreads);
            }

            stopWatch.stop();
            LOGGER.debug("Tracker Time Elapsed: " + TimeUnit.MILLISECONDS.toSeconds(stopWatch.getTime()) + " seconds.");
            stopWatch.reset();
            try {
                LOGGER.debug("Tracker sleeping");
                TimeUnit.SECONDS.sleep(trackingPollingInterval);
            } catch (InterruptedException e) {
                break;
            }
        }

    }
}
