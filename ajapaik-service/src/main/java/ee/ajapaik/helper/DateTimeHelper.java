package ee.ajapaik.helper;

import org.springframework.stereotype.Service;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class DateTimeHelper {

    public static final ZoneId TIMEZONE = ZoneId.of("Europe/Tallinn");

    public ZonedDateTime now() {
        return ZonedDateTime.now(TIMEZONE);
    }

    public String formatDate(ZonedDateTime dateTime) {
        return dateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
    }
}
