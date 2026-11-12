package ee.aleksale.remindly.core.model.type;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.List;

@Getter
@RequiredArgsConstructor
public enum EventType {
  BIO_WASTE_COLLECTION(ReminderType.GARBAGE_COLLECTION, DeliveryChannel.CALENDAR),
  MIXED_WASTE_COLLECTION(ReminderType.GARBAGE_COLLECTION, DeliveryChannel.CALENDAR),
  PACKAGING_WASTE_COLLECTION(ReminderType.GARBAGE_COLLECTION, DeliveryChannel.CALENDAR),

  GARBAGE_SCHEDULE_RESET(ReminderType.GARBAGE_COLLECTION, DeliveryChannel.NTFY),
  GARBAGE_SCHEDULE_FETCHED(ReminderType.GARBAGE_COLLECTION, DeliveryChannel.NTFY),

  ADHOC(ReminderType.ADHOC, DeliveryChannel.NTFY),
  SNOOZE(ReminderType.ADHOC, DeliveryChannel.NTFY),

  REMINDLY_APP_ERROR(ReminderType.ERRORS, DeliveryChannel.NTFY);

  public final ReminderType reminderType;
  public final DeliveryChannel deliveryChannel;

  public static List<EventType> byChannel(DeliveryChannel channel) {
    return Arrays.stream(values())
            .filter(type -> type.deliveryChannel == channel)
            .toList();
  }
}
