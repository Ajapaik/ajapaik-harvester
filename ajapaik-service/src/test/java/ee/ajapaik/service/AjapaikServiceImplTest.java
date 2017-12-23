package ee.ajapaik.service;

import ee.ajapaik.helper.DateTimeHelper;
import org.junit.Test;

import java.time.ZonedDateTime;

import static ee.ajapaik.helper.DateTimeHelper.TIMEZONE;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

public class AjapaikServiceImplTest {

    @Test
    public void getDateFromFileName() throws Exception {
        AjapaikServiceImpl ajapaikService = new AjapaikServiceImpl();
        assertEquals("2017-12-16", ajapaikService.getDateFromFileName("failed-sets.log.2017-12-16"));
    }

    @Test
    public void getDateFromFileName_todaysFile() throws Exception {
        AjapaikServiceImpl ajapaikService = new AjapaikServiceImpl();
        ajapaikService.dateTimeHelper = mock(DateTimeHelper.class);
        ZonedDateTime now = ZonedDateTime.of(2017, 12, 23, 15, 45, 6, 0, TIMEZONE);
        doReturn(now).when(ajapaikService.dateTimeHelper).now();
        doReturn("2017-12-23").when(ajapaikService.dateTimeHelper).formatDate(now);
        assertEquals("2017-12-23", ajapaikService.getDateFromFileName("failed-sets.log"));
    }
}