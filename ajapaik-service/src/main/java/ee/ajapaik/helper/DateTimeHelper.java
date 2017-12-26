package ee.ajapaik.helper;

import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class DateTimeHelper {

    public static final ZoneId TIMEZONE = ZoneId.of("Europe/Tallinn");
    private DateTimeFormatter dateTimeFormatter  = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public ZonedDateTime now() {
        return ZonedDateTime.now(TIMEZONE);
    }

    public String formatDate(ZonedDateTime dateTime) {
        return dateTime.format(dateTimeFormatter);
    }

    public LocalDate parseDate(String date) {
        return LocalDate.parse(date, dateTimeFormatter);
    }
}
