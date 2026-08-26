package ee.aleksale.remindly.core.client;

import static java.util.Map.entry;

import ee.aleksale.remindly.core.exception.RemindlyException;
import ee.aleksale.remindly.core.model.type.EventType;
import ee.aleksale.remindly.core.property.RemindlyAppProperties;
import ee.aleksale.remindly.utils.EmojiUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class NtfyClient {

  private static final String NTFY_EMOJIS_HEADER = "Tags";
  private static final String NTFY_TITLE_HEADER = "Title";

  private final RestTemplate restTemplate;
  private final RemindlyAppProperties property;

  public NtfyClient(RestTemplate restTemplate, RemindlyAppProperties property) {
    this.restTemplate = restTemplate;
    this.property = property;
  }

  private void sendNotification(NtfyRequestPayload payload) {
    final var eventType = payload.eventType();
    final var reminderType = eventType.getReminderType();
    final var reminder = property.getReminders(reminderType);

    if (!reminder.isEnabled()) {
      throw new RemindlyException("Notification for event type " + eventType + " is disabled.");
    }

    final var headers = new HttpHeaders(
            MultiValueMap.fromSingleValue(Map.ofEntries(
                    entry(NTFY_EMOJIS_HEADER, payload.emojis),
                    entry(NTFY_TITLE_HEADER, reminderType.toString())
            )));
    final var request = new HttpEntity<>(payload.message(), headers);

    restTemplate.postForEntity(
            property.getNtfyServer() + "/" + reminder.getTopic(),
            request,
            Void.class
    );
  }

  public NtfyRequestBuilder notification(EventType eventType) {
    return new NtfyRequestBuilder(this, eventType);
  }

  public static class NtfyRequestBuilder {

    private final NtfyClient client;
    private final EventType eventType;

    private String message;
    private List<String> emojis;

    private NtfyRequestBuilder(NtfyClient client, EventType eventType) {
      this.client = client;
      this.eventType = eventType;
    }

    public NtfyRequestBuilder withMessage(String message) {
      this.message = message;
      return this;
    }

    public NtfyRequestBuilder withDefaultEmojis() {
      this.emojis = EmojiUtils.getEmojisForEventType(eventType);
      return this;
    }

    public NtfyRequestBuilder withEmojis(List<String> emojis) {
      this.emojis = emojis;
      return this;
    }

    public void send() {
      client.sendNotification(new NtfyRequestPayload(eventType, message, String.join(",", emojis)));
    }
  }

  private record NtfyRequestPayload(EventType eventType, String message, String emojis) {}

}
