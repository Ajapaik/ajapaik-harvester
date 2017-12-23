package ee.ajapaik.helper;

import org.junit.Test;

import java.time.ZonedDateTime;

import static ee.ajapaik.helper.DateTimeHelper.TIMEZONE;
import static org.junit.Assert.assertEquals;

public class DateTimeHelperTest {

    DateTimeHelper dateTimeHelper = new DateTimeHelper();

    @Test
    public void formatDate() {
        assertEquals("2017-12-23", dateTimeHelper.formatDate(ZonedDateTime.of(2017, 12, 23, 14, 36, 25, 4, TIMEZONE)));
    }
}