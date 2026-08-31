package ee.aleksale.remindly.utils;

import ee.aleksale.remindly.core.model.type.EventType;

import java.util.List;

public class EmojiUtils {
  public static final String SEEDLING = "seedling"; // 🌱
  public static final String LEAVES = "leaves"; // 🍃

  public static final String PACKAGE = "package"; // 📦
  public static final String LABEL = "label"; // 🏷️

  public static final String WASTEBASKET = "wastebasket"; // 🗑️
  public static final String RECYCLE = "recycle"; // ♻️️

  public static final String MEMO = "memo"; // 📝
  public static final String BELL = "bell"; // 🔔

  public static final String ZZZ = "zzz"; // 💤
  public static final String SLEEPING = "sleeping"; // 😴

  public static final String ROTATING_LIGHT = "rotating_light"; // 🚨
  public static final String ARROWS_COUNTERCLOCKWISE = "arrows_counterclockwise"; // 🔄

  public static final String PUSHPIN = "pushpin "; // 📌

  public static List<String> getEmojisForEventType(EventType eventType) {
    return switch (eventType) {
      case BIO_WASTE_COLLECTION -> List.of(SEEDLING, LEAVES);
      case MIXED_WASTE_COLLECTION -> List.of(WASTEBASKET, RECYCLE);
      case PACKAGING_WASTE_COLLECTION -> List.of(PACKAGE, LABEL);
      case GARBAGE_SCHEDULE_RESET -> List.of(ARROWS_COUNTERCLOCKWISE);
      case ADHOC -> List.of(MEMO, BELL);
      case SNOOZE -> List.of(ZZZ, SLEEPING);
      case REMINDLY_APP_ERROR -> List.of(ROTATING_LIGHT);
      default -> List.of(PUSHPIN);
    };
  }

}
