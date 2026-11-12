package ee.aleksale.remindly.utils;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Emoji {
  SEEDLING("seedling", "🌱"),
  LEAVES("leaves", "🍃"),

  PACKAGE("package", "📦"),
  LABEL("label", "🏷️"),

  WASTEBASKET("wastebasket", "🗑️"),
  RECYCLE("recycle", "♻️"),

  MEMO("memo", "📝"),
  BELL("bell", "🔔"),

  ZZZ("zzz", "💤"),
  SLEEPING("sleeping", "😴"),

  ROTATING_LIGHT("rotating_light", "🚨"),
  ARROWS_COUNTERCLOCKWISE("arrows_counterclockwise", "🔄"),

  PUSHPIN("pushpin", "📌"),
  ENVELOPE_WITH_ARROW("envelope_with_arrow", "📩"),

  INBOX_TRAY("inbox_tray", "📥"),
  EMAIL("email", "📧");

  private final String shortcode;
  private final String unicode;
}
