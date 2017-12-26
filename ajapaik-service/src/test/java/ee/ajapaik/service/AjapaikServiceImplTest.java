package ee.ajapaik.service;

import ee.ajapaik.helper.DateTimeHelper;
import org.junit.Before;
import org.junit.Test;

import java.time.LocalDate;
import java.time.ZonedDateTime;

import static ee.ajapaik.helper.DateTimeHelper.TIMEZONE;
import static org.junit.Assert.*;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

public class AjapaikServiceImplTest {

    private AjapaikServiceImpl service;

    @Before
    public void setUp() throws Exception {
        service = new AjapaikServiceImpl();
        service.dateTimeHelper = mock(DateTimeHelper.class);
    }

    @Test
    public void getDateFromFileName() throws Exception {
        assertEquals("2017-12-16", service.getDateFromFileName("failed-sets.log.2017-12-16"));
    }

    @Test
    public void getDateFromFileName_todaysFile() throws Exception {
        ZonedDateTime now = ZonedDateTime.of(2017, 12, 23, 15, 45, 6, 0, TIMEZONE);
        doReturn(now).when(service.dateTimeHelper).now();
        doReturn("2017-12-23").when(service.dateTimeHelper).formatDate(now);
        assertEquals("2017-12-23", service.getDateFromFileName("failed-sets.log"));
    }

    @Test
    public void shouldShow_yearOld() throws Exception {
        String dateString = "2016-12-26";
        doReturn(ZonedDateTime.of(2017, 12, 26, 18, 25, 54, 0, TIMEZONE)).when(service.dateTimeHelper).now();
        doReturn(LocalDate.of(2016, 12, 26)).when(service.dateTimeHelper).parseDate(dateString);

        assertTrue(service.shouldNotShow(dateString));
    }

    @Test
    public void shouldShow_oneMonthOld() throws Exception {
        String dateString = "2017-11-26";
        doReturn(ZonedDateTime.of(2017, 12, 26, 18, 25, 54, 0, TIMEZONE)).when(service.dateTimeHelper).now();
        doReturn(LocalDate.of(2017, 11, 26)).when(service.dateTimeHelper).parseDate(dateString);

        assertFalse(service.shouldNotShow(dateString));
    }

    @Test
    public void shouldShow_ThreeMonthsOld() throws Exception {
        String dateString = "2017-09-26";
        doReturn(ZonedDateTime.of(2017, 12, 26, 18, 25, 54, 0, TIMEZONE)).when(service.dateTimeHelper).now();
        doReturn(LocalDate.of(2017, 9, 26)).when(service.dateTimeHelper).parseDate(dateString);

        assertFalse(service.shouldNotShow(dateString));
    }

    @Test
    public void shouldShow_ThreeMonthsAndOneDayOld() throws Exception {
        String dateString = "2017-09-26";
        doReturn(ZonedDateTime.of(2017, 12, 26, 18, 25, 54, 0, TIMEZONE)).when(service.dateTimeHelper).now();
        doReturn(LocalDate.of(2017, 9, 25)).when(service.dateTimeHelper).parseDate(dateString);

        assertTrue(service.shouldNotShow(dateString));
    }
}