package ee.aleksale.remindly.core.client;

import ee.aleksale.remindly.core.exception.RemindlyException;
import ee.aleksale.remindly.core.model.dto.NtfyAction;
import ee.aleksale.remindly.core.model.dto.NtfyRequestPayload;
import ee.aleksale.remindly.core.model.type.EventType;
import ee.aleksale.remindly.core.property.RemindlyAppProperties;
import ee.aleksale.remindly.core.service.NtfyHeaderService;
import ee.aleksale.remindly.utils.EmojiUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Slf4j
@Service
public class NtfyClient {

  private final RestTemplate restTemplate;
  private final RemindlyAppProperties property;
  private final NtfyHeaderService ntfyHeaderService;

  public NtfyClient(RestTemplate restTemplate, RemindlyAppProperties property, NtfyHeaderService ntfyHeaderService) {
    this.restTemplate = restTemplate;
    this.property = property;
    this.ntfyHeaderService = ntfyHeaderService;
  }

  private void sendNotification(NtfyRequestPayload payload) {
    final var eventType = payload.eventType();
    final var reminderType = eventType.getReminderType();
    final var reminder = property.getReminders(reminderType);

    if (!reminder.isEnabled()) {
      throw new RemindlyException("Notification for event type " + eventType + " is disabled.");
    }

    var headers = ntfyHeaderService.getHeaders(payload);

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

    private String title;
    private String message;
    private List<String> emojis;
    private List<NtfyAction> ntfyActions;

    private NtfyRequestBuilder(NtfyClient client, EventType eventType) {
      this.client = client;
      this.eventType = eventType;
    }

    public NtfyRequestBuilder withTitle(String title) {
      this.title = title;
      return this;
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

    public NtfyRequestBuilder withAction(NtfyAction action) {
      this.ntfyActions = List.of(action);
      return this;
    }

    public NtfyRequestBuilder withActions(List<NtfyAction> actions) {
      this.ntfyActions = actions;
      return this;
    }

    public void send() {
      client.sendNotification(new NtfyRequestPayload(
              eventType,
              title,
              message,
              ntfyActions,
              emojis == null ? null : String.join(",", emojis)
      ));
    }
  }
}
