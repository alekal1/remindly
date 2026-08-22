package ee.aleksale.remindly.core.client;

import ee.aleksale.remindly.core.model.type.EventType;
import ee.aleksale.remindly.core.model.type.NtfyTopic;
import ee.aleksale.remindly.core.property.NftyProperty;
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
  private final NftyProperty property;
  private final EnumMap<EventType, NtfyTopic> eventTypeToTopic;

  public NtfyClient(RestTemplate restTemplate, NftyProperty property) {
    this.restTemplate = restTemplate;
    this.property = property;

    this.eventTypeToTopic = new EnumMap<>(
            Map.of(
                    EventType.BIO_WASTE_COLLECTION, NtfyTopic.GARBAGE_COLLECTION,
                    EventType.MIXED_WASTE_COLLECTION, NtfyTopic.GARBAGE_COLLECTION,
                    EventType.PACKAGING_WASTE_COLLECTION, NtfyTopic.GARBAGE_COLLECTION,
                    EventType.GARBAGE_SCHEDULE_RESET, NtfyTopic.GARBAGE_COLLECTION,
                    EventType.REMINDLY_APP_ERROR, NtfyTopic.ERRORS
            )
    );
  }

  public void sendNotification(EventType eventType, String message) {
    final var topicName = eventTypeToTopic.get(eventType);
    final var topic = property.getTopic(topicName);

    final var response = restTemplate.postForEntity(
            String.format(NTFY_BASE_URL, topic),
            message,
            Void.class
    );

    if (response.getStatusCode().isError()) {
      log.error("Failed to send notification for event type {}: {}", eventType, response.getStatusCode());
    }
  }

}
