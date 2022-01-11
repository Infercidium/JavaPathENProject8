package tourGuide.controller;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;
import rewardCentral.RewardCentral;

import java.util.UUID;

import static org.junit.Assert.assertEquals;

@SpringBootTest(classes = {RewardCentralController.class})
@RunWith(SpringRunner.class)
public class RewardCentralControllerTest {

    @MockBean
    private RewardCentral rewardCentral;

    @Autowired
    private RewardCentralController rewardCentralController;

    @Test
    public void getAttractionRewardPoints() {
        Mockito.when(rewardCentral.getAttractionRewardPoints(Mockito.isA(UUID.class), Mockito.isA(UUID.class))).thenReturn(10);
        int result = rewardCentralController.getAttractionRewardPoints(UUID.randomUUID(), UUID.randomUUID());
        assertEquals(10, result);
    }
}