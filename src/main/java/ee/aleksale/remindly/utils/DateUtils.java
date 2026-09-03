package ee.aleksale.remindly.utils;

import lombok.experimental.UtilityClass;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@UtilityClass
public class DateUtils {

  public static LocalDate convertToLocalDate(String date, String currentDatePattern) {
    var formatter = DateTimeFormatter.ofPattern(currentDatePattern);
    return LocalDate.parse(date, formatter);
  }
}
