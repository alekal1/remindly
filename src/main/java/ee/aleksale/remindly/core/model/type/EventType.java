package ee.aleksale.remindly.core.model.type;

import lombok.Getter;

import java.util.Arrays;
import java.util.List;

@Getter
public enum EventType {
  BIO_WASTE_COLLECTION(DeliveryChannel.CALENDAR),
  MIXED_WASTE_COLLECTION(DeliveryChannel.CALENDAR),
  PACKAGING_WASTE_COLLECTION(DeliveryChannel.CALENDAR),

  GARBAGE_SCHEDULE_RESET(ReminderType.CONFIRMATION),
  GARBAGE_SCHEDULE_FETCHED(ReminderType.CONFIRMATION),

  ADHOC(ReminderType.ADHOC),
  SNOOZE(ReminderType.ADHOC),

  REMINDLY_APP_ERROR(ReminderType.ERRORS);

  public final ReminderType reminderType;
  public final DeliveryChannel deliveryChannel;

  EventType(ReminderType reminderType) {
    this(reminderType, DeliveryChannel.NTFY);
  }

  EventType(DeliveryChannel deliveryChannel) {
    this(null, deliveryChannel);
  }

  EventType(ReminderType reminderType, DeliveryChannel deliveryChannel) {
    this.reminderType = reminderType;
    this.deliveryChannel = deliveryChannel;
  }

  public static List<EventType> byChannel(DeliveryChannel channel) {
    return Arrays.stream(values())
            .filter(type -> type.deliveryChannel == channel)
            .toList();
  }
}
