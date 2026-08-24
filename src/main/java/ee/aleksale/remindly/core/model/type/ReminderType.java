package ee.aleksale.remindly.core.model.type;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ReminderType {
  ERRORS("errors"),
  GARBAGE_COLLECTION("garbage-collection"),
  ONE_TIME_REMINDER("one-time-reminder");

  private final String configKey;
}
