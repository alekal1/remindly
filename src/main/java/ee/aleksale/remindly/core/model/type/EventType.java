package ee.aleksale.remindly.core.model.type;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum EventType {
  BIO_WASTE_COLLECTION("Bio waste collection is scheduled for tomorrow."),
  MIXED_WASTE_COLLECTION("Mixed waste collection is scheduled for tomorrow."),
  PACKAGING_WASTE_COLLECTION("Packaging waste collection is scheduled for tomorrow."),
  GARBAGE_SCHEDULE_RESET("Garbage collection schedule has been reset."),

  ADHOC("Adhoc reminder created."),

  REMINDLY_APP_ERROR("Unexpected application error.");

  private final String notificationMessage;
}
