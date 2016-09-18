package ee.ajapaik.harvester;

import ee.ajapaik.model.InfoSystem;
import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class HarvestTaskTest {

  private HarvestTask harvestTask;
  private Date lastHarvest = new Date(1474192737493L);

  @Before
  public void setUp() throws Exception {
    harvestTask = spy(new MuisHarvestTask());
    harvestTask.logger = mock(Logger.class);
  }

  @Test
  public void iterateSet() throws IOException, JAXBException {
    Map<String, String> params = new HashMap<String, String>();
    harvestTask.format = "format";
    doNothing().when(harvestTask).iterateRecords(params);

    harvestTask.iterateSet(params, lastHarvest, "setSpecValue");

    verify(harvestTask.logger).debug("Set iterated: setSpecValue");
    assertEquals("setSpecValue", params.get("set"));
    assertEquals("format", params.get("metadataPrefix"));
    assertEquals("2016-09-18", params.get("from"));
  }

  @Test
  public void iterateSet_noLastHarvestTime() throws IOException, JAXBException {
    Map<String, String> params = new HashMap<String, String>();
    doNothing().when(harvestTask).iterateRecords(params);

    harvestTask.iterateSet(params, null, null);

    assertFalse(params.containsKey("from"));
  }

  @Test
  public void iterateSet_noSet() throws IOException, JAXBException {
    Map<String, String> params = new HashMap<String, String>();
    doNothing().when(harvestTask).iterateRecords(params);

    harvestTask.iterateSet(params, null, null);

    assertFalse(params.containsKey("set"));
  }

  @Test
  public void iterateSets_multipleSets() throws IOException, JAXBException {
    Map<String, String> params = new HashMap<String, String>();
    Map<String, String> sets = new HashMap<String, String>() {{
      put("S1", "set1");
      put("S2", "set2");
      put("S3", "set3");
    }};
    harvestTask.setSets(sets);
    harvestTask.infoSystem = new InfoSystem();
    doNothing().when(harvestTask).iterateSet(anyMap(), any(Date.class), anyString());

    harvestTask.iterateSets(params, lastHarvest);

    verify(harvestTask).iterateSet(params, lastHarvest, "S1");
    verify(harvestTask).iterateSet(params, lastHarvest, "S2");
    verify(harvestTask).iterateSet(params, lastHarvest, "S3");
  }

  @Test
  public void iterateSets_continuesToNextSetIfIterateSetMethodThrewJAXBExceptionForPreviousSet() throws IOException, JAXBException {
    Map<String, String> params = spy(new HashMap<String, String>());
    Map<String, String> sets = new HashMap<String, String>() {{
      put("S1", "set1");
      put("S2", "set2");
      put("S3", "set3");
    }};
    harvestTask.setSets(sets);
    harvestTask.infoSystem = new InfoSystem();
    doThrow(JAXBException.class).when(params).put("set", "S2");
    doNothing().when(harvestTask).iterateRecords(anyMap());

    harvestTask.iterateSets(params, lastHarvest);

    verify(harvestTask).iterateSet(params, lastHarvest, "S1");
    verify(harvestTask).iterateSet(params, lastHarvest, "S2");
    verify(harvestTask).iterateSet(params, lastHarvest, "S3");
    verify(harvestTask.logger).error(eq("Error parsing stream"), any(JAXBException.class));
    verify(harvestTask.logger).error("Failed to import set! Set = S2");
    verify(harvestTask.logger).debug("Set iterated: S1");
    verify(harvestTask.logger).debug("Set iterated: S3");
  }

  @Test
  public void iterateSets_skipIgnoredSet() throws IOException, JAXBException {
    InfoSystem infoSystem = new InfoSystem();
    infoSystem.setIgnoreSet("S2,S4");
    harvestTask.infoSystem = infoSystem;
    Map<String, String> params = new HashMap<String, String>();
    Map<String, String> sets = new HashMap<String, String>() {{
      put("S1", "set1");
      put("S2", "set2");
      put("S3", "set3");
      put("S4", "set4");
    }};
    harvestTask.setSets(sets);
    doNothing().when(harvestTask).iterateSet(anyMap(), any(Date.class), anyString());

    harvestTask.iterateSets(params, lastHarvest);

    verify(harvestTask).iterateSet(params, lastHarvest, "S1");
    verify(harvestTask).iterateSet(params, lastHarvest, "S3");
    verify(harvestTask, never()).iterateSet(anyMap(), any(Date.class), eq("S2"));
    verify(harvestTask.logger).debug("Set ignored: S2");
    verify(harvestTask, never()).iterateSet(anyMap(), any(Date.class), eq("S4"));
    verify(harvestTask.logger).debug("Set ignored: S4");
  }

  @Test
  public void iterateSets_noSetsDefinedRunsIterateSetOnce() throws IOException, JAXBException {
    harvestTask.infoSystem = new InfoSystem();
    Map<String, String> params = new HashMap<String, String>();
    doNothing().when(harvestTask).iterateSet(anyMap(), any(Date.class), anyString());

    harvestTask.iterateSets(params, lastHarvest);

    verify(harvestTask).iterateSet(params, lastHarvest, null);
  }
}