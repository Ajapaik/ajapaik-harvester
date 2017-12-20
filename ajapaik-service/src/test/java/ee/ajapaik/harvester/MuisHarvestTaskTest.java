package ee.ajapaik.harvester;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class MuisHarvestTaskTest {

    @Test
    public void getMediaId() throws Exception {
        MuisHarvestTask muisHarvestTask = new MuisHarvestTask();
        String mediaId = muisHarvestTask.getMediaId("http://opendata.muis.ee/dhmedia/2d692104-f4e6-4170-b496-42bda92aea92");

        assertEquals("2d692104-f4e6-4170-b496-42bda92aea92", mediaId);
    }
}