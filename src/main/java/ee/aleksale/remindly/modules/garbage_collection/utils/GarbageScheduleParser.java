package ee.aleksale.remindly.modules.garbage_collection.utils;

import ee.aleksale.remindly.modules.garbage_collection.dto.GarbageCollectionSchedule;
import lombok.experimental.UtilityClass;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;

@UtilityClass
public class GarbageScheduleParser {

  private static final DateTimeFormatter DATE_FORMAT =
          DateTimeFormatter.ofPattern("dd.MM.yyyy");

  private static final Pattern HEADER_PATTERN = Pattern.compile(
          "^(.+?)\\s*\\|\\s*([0-9.]+m3)\\s+(.+?)\\s+-\\s+\\d+tk$",
          Pattern.MULTILINE
  );

  private static final Pattern DATE_PATTERN = Pattern.compile(
          "\\b\\d{2}\\.\\d{2}\\.\\d{4}\\b"
  );

  public static List<GarbageCollectionSchedule> parse(String text) {
    var headers = collectHeader(text);
    var result = new ArrayList<GarbageCollectionSchedule>();

    for (int i = 0; i < headers.size(); i++) {
      var header = headers.get(i);
      var block = extractBlockBetweenHeaders(text, headers, i);
      var type = mapType(header.group(3));
      var dates = extractDatesFromBlock(block);

      result.add(GarbageCollectionSchedule.builder()
              .type(type)
              .dates(dates)
              .build());
    }

    return result;
  }

  private static String extractBlockBetweenHeaders(String text, ArrayList<MatchResult> headers, int index) {
    int start = headers.get(index).end();
    int end = index + 1 < headers.size()
            ? headers.get(index + 1).start()
            : text.length();
    return text.substring(start, end);
  }

  private static List<LocalDate> extractDatesFromBlock(String block) {
    var dates = new ArrayList<LocalDate>();
    var dateMatcher = DATE_PATTERN.matcher(block);

    while (dateMatcher.find()) {
      dates.add(LocalDate.parse(dateMatcher.group(), DATE_FORMAT));
    }

    return dates;
  }

  private ArrayList<MatchResult> collectHeader(String text) {
    var headers = new ArrayList<MatchResult>();

    var matcher = HEADER_PATTERN.matcher(text);

    while (matcher.find()) {
      headers.add(matcher.toMatchResult());
    }

    return headers;
  }

  private GarbageCollectionSchedule.GarbageType mapType(String type) {
    return switch (type.toLowerCase()) {
      case "biolagunevad köögi-ja sööklajäätmed" -> GarbageCollectionSchedule.GarbageType.BIO;
      case "segaolmejäätmed" -> GarbageCollectionSchedule.GarbageType.MIXED;
      case "paber ja papp" -> GarbageCollectionSchedule.GarbageType.PACKAGING;
      default -> throw new IllegalArgumentException("Unknown garbage type: " + type);
    };
  }
}
