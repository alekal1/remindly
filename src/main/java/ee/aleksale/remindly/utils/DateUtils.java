package ee.aleksale.remindly.utils;

import jakarta.annotation.Nullable;
import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.StringUtils;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@UtilityClass
public class DateUtils {

  @Nullable
  public static LocalDate convertToLocalDate(String date,
                                             String currentDatePattern) {
    if (StringUtils.isBlank(date) || StringUtils.isBlank(currentDatePattern)) {
      return null;
    }

    var formatter = DateTimeFormatter.ofPattern(currentDatePattern);
    return LocalDate.parse(date, formatter);
  }
}
