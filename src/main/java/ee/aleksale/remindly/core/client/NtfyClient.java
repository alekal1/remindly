package ee.aleksale.remindly.core.client;

import ee.aleksale.remindly.core.exception.RemindlyException;
import ee.aleksale.remindly.core.model.type.EventType;
import ee.aleksale.remindly.core.model.type.ReminderType;
import ee.aleksale.remindly.core.property.RemindlyProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.EnumMap;
import java.util.Map;

@Slf4j
@Service
public class NtfyClient {

  private static final String NTFY_BASE_URL = "http://ntfy.sh/%s";

  private final RestTemplate restTemplate;
  private final RemindlyProperties property;
  private final EnumMap<EventType, ReminderType> eventTypeToTopic;

  public NtfyClient(RestTemplate restTemplate, RemindlyProperties property) {
    this.restTemplate = restTemplate;
    this.property = property;

    this.eventTypeToTopic = new EnumMap<>(
            Map.of(
                    EventType.BIO_WASTE_COLLECTION, ReminderType.GARBAGE_COLLECTION,
                    EventType.MIXED_WASTE_COLLECTION, ReminderType.GARBAGE_COLLECTION,
                    EventType.PACKAGING_WASTE_COLLECTION, ReminderType.GARBAGE_COLLECTION,
                    EventType.GARBAGE_SCHEDULE_RESET, ReminderType.GARBAGE_COLLECTION,
                    EventType.REMINDLY_APP_ERROR, ReminderType.ERRORS
            )
    );
  }

  public void sendNotification(EventType eventType, String message) {
    final var notificationType = eventTypeToTopic.get(eventType);
    final var topic = property.getReminder(notificationType);

    if (!topic.isEnabled()) {
      throw new RemindlyException("Notification for event type " + eventType + " is disabled.");
    }

    final var response = restTemplate.postForEntity(
            String.format(NTFY_BASE_URL, topic.getTopic()),
            message,
            Void.class
    );

    if (response.getStatusCode().isError()) {
      log.error("Failed to send notification for event type {}: {}", eventType, response.getStatusCode());
    }
  }

}
