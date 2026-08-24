package ee.aleksale.remindly.core.model.type;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum EventType {
  BIO_WASTE_COLLECTION,
  MIXED_WASTE_COLLECTION,
  PACKAGING_WASTE_COLLECTION,
  GARBAGE_SCHEDULE_RESET,

  ADHOC,

  REMINDLY_APP_ERROR
}
