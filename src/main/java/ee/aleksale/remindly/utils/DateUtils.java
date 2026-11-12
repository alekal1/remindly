package ee.aleksale.remindly.utils;

import jakarta.annotation.Nullable;
import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.StringUtils;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@UtilityClass
public class DateUtils {

  public static final String TIMEZONE = "Europe/Tallinn";

  @Nullable
  public static LocalDate convertToLocalDate(String date,
                                             String currentDatePattern) {
    if (StringUtils.isBlank(date) || StringUtils.isBlank(currentDatePattern)) {
      return null;
    }

    var formatter = DateTimeFormatter.ofPattern(currentDatePattern);
    return LocalDate.parse(date, formatter);
  }

  public static LocalDateTime startOfDayDaysAgo(Clock clock, long days) {
    return LocalDate.now(clock).minusDays(days).atStartOfDay();
  }
}
