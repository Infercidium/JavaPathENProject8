package tourGuide.controller;

import com.jsoniter.output.JsonStream;
import gpsUtil.GpsUtil;
import gpsUtil.location.Attraction;
import gpsUtil.location.Location;
import gpsUtil.location.VisitedLocation;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static org.junit.Assert.assertEquals;

@SpringBootTest(classes = {GpsUtilController.class})
@RunWith(SpringRunner.class)
public class GpsUtilControllerTest {

    @MockBean
    private GpsUtil gpsUtil;

    @Autowired
    private GpsUtilController gpsUtilController;

    UUID uuid = UUID.randomUUID();
    Location location = new Location(10, 10);
    VisitedLocation visitedLocation = new VisitedLocation(uuid, location, new Date());
    Attraction attraction = new Attraction("name", "city", "state", 15, 15);

    @Test
    public void getUserLocation() {
        Mockito.when(gpsUtil.getUserLocation(Mockito.isA(UUID.class))).thenReturn(visitedLocation);
        VisitedLocation result = gpsUtilController.getUserLocation(uuid.toString());
        assertEquals(JsonStream.serialize(visitedLocation), JsonStream.serialize(result));
    }

    @Test
    public void getAttractions() {
        Mockito.when(gpsUtil.getAttractions()).thenReturn(Collections.singletonList(attraction));
        List<Attraction> result = gpsUtilController.getAttractions();
        assertEquals(1, result.size());
        assertEquals(JsonStream.serialize(attraction), JsonStream.serialize(result.get(0)));

    }
}