package tourGuide.controller;

import com.jsoniter.output.JsonStream;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;
import tripPricer.Provider;
import tripPricer.TripPricer;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.Assert.assertEquals;

@SpringBootTest(classes = {TripPricerController.class})
@RunWith(SpringRunner.class)
public class TripPricerControllerTest {

    @MockBean
    private TripPricer tripPricer;

    @Autowired
    private TripPricerController tripPricerController;

    private UUID uuid = UUID.randomUUID();
    private Provider provider = new Provider(uuid, "name", 100);

    @Test
    public void getPricer() {
        Mockito.when(tripPricer.getPrice(Mockito.isA(String.class), Mockito.isA(UUID.class), Mockito.isA(Integer.class), Mockito.isA(Integer.class), Mockito.isA(Integer.class), Mockito.isA(Integer.class))).thenReturn(Collections.singletonList(provider));
        List<Provider> result = tripPricerController.getPricer("apiKey", uuid, 1, 0, 1, 100);
        assertEquals(1, result.size());
        assertEquals(JsonStream.serialize(provider), JsonStream.serialize(result.get(0)));
    }
}