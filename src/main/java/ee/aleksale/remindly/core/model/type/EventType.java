package ee.aleksale.remindly.core.model.type;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum EventType {
  BIO_WASTE_COLLECTION(ReminderType.GARBAGE_COLLECTION),
  MIXED_WASTE_COLLECTION(ReminderType.GARBAGE_COLLECTION),
  PACKAGING_WASTE_COLLECTION(ReminderType.GARBAGE_COLLECTION),

  GARBAGE_SCHEDULE_RESET(ReminderType.GARBAGE_COLLECTION),
  GARBAGE_SCHEDULE_FETCHED(ReminderType.GARBAGE_COLLECTION),

  ADHOC(ReminderType.ADHOC),
  SNOOZE(ReminderType.ADHOC),

  REMINDLY_APP_ERROR(ReminderType.ERRORS);

  public final ReminderType reminderType;
}
