package ee.ajapaik.harvester;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class MuisHarvestTaskTest {

    @Test
    public void getMediaId() throws Exception {
        MuisHarvestTask muisHarvestTask = new MuisHarvestTask();
        Integer mediaId = muisHarvestTask.getMediaId("http://opendata.muis.ee/dhmedia/2d692104-f4e6-4170-b496-42bda92aea92");

        assertEquals(new Integer(1153906108), mediaId);
    }
}