package ee.aleksale.remindly.utils;

import ee.aleksale.remindly.core.model.type.EventType;

import java.util.List;
import java.util.stream.Collectors;

import static ee.aleksale.remindly.utils.Emoji.ARROWS_COUNTERCLOCKWISE;
import static ee.aleksale.remindly.utils.Emoji.BELL;
import static ee.aleksale.remindly.utils.Emoji.ENVELOPE_WITH_ARROW;
import static ee.aleksale.remindly.utils.Emoji.LABEL;
import static ee.aleksale.remindly.utils.Emoji.LEAVES;
import static ee.aleksale.remindly.utils.Emoji.MEMO;
import static ee.aleksale.remindly.utils.Emoji.PACKAGE;
import static ee.aleksale.remindly.utils.Emoji.PUSHPIN;
import static ee.aleksale.remindly.utils.Emoji.RECYCLE;
import static ee.aleksale.remindly.utils.Emoji.ROTATING_LIGHT;
import static ee.aleksale.remindly.utils.Emoji.SEEDLING;
import static ee.aleksale.remindly.utils.Emoji.WASTEBASKET;

public class EmojiUtils {

  public static List<Emoji> getEmojisForEventType(EventType eventType) {
    return switch (eventType) {
      case BIO_WASTE_COLLECTION -> List.of(SEEDLING, LEAVES);
      case MIXED_WASTE_COLLECTION -> List.of(WASTEBASKET, RECYCLE);
      case PACKAGING_WASTE_COLLECTION -> List.of(PACKAGE, LABEL);
      case GARBAGE_SCHEDULE_RESET -> List.of(ARROWS_COUNTERCLOCKWISE);
      case GARBAGE_SCHEDULE_FETCHED -> List.of(ENVELOPE_WITH_ARROW);
      case ADHOC -> List.of(MEMO, BELL);
      case SNOOZE -> List.of(PUSHPIN, MEMO, BELL);
      case REMINDLY_APP_ERROR -> List.of(ROTATING_LIGHT);
    };
  }

  public static String toShortcodes(List<Emoji> emojis) {
    return emojis.stream().map(Emoji::getShortcode).collect(Collectors.joining(","));
  }

  public static String toUnicode(List<Emoji> emojis) {
    return emojis.stream().map(Emoji::getUnicode).collect(Collectors.joining());
  }

}
