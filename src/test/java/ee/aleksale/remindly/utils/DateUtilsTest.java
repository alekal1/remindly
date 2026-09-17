package ee.aleksale.remindly.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.LocalDate;
import java.util.stream.Stream;

public class DateUtilsTest {

  @Test
  void shouldConvertToLocalDate_whenValidStringAndPatternProvided() {
    final var localDateString = "2024-06-17";
    final var currentDatePattern = "yyyy-MM-dd";

    final var result = DateUtils.convertToLocalDate(localDateString, currentDatePattern);

    assertNotNull(result);
    assertEquals(LocalDate.of(2024, 6, 17), result);
  }

  private static Stream<Arguments> blankValues(){
    return Stream.of(
            null,
            Arguments.of(""),
            Arguments.of(" "),
            Arguments.of("  "),
            Arguments.of("   ")
    );
  }

  @ParameterizedTest
  @MethodSource("blankValues")
  void shouldConvertToLocalDate_whenBlankDateProvided(String blankValue) {
    final var currentDatePattern = "yyyy-MM-dd";

    final var result = DateUtils.convertToLocalDate(blankValue, currentDatePattern);

    assertNull(result);
  }

  @ParameterizedTest
  @MethodSource("blankValues")
  void shouldConvertToLocalDate_whenBlankDatePatternProvided(String blankValue) {
    final var date = "2022-12-12";

    final var result = DateUtils.convertToLocalDate(date, blankValue);

    assertNull(result);
  }
}
