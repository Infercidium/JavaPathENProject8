package unitaire.proxy;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import tourGuide.proxy.TripPricerProxy;

import static org.junit.Assert.*;

@SpringBootTest(classes = {TripPricerProxy.class})
public class TripPricerProxyTest {

    @Test
    public void price() {
    }
}