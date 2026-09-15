package ee.aleksale.remindly.core.client;

import ee.aleksale.remindly.core.exception.RemindlyException;
import ee.aleksale.remindly.core.model.dto.NtfyAction;
import ee.aleksale.remindly.core.model.dto.NtfyRequestPayload;
import ee.aleksale.remindly.core.model.type.ReminderType;
import ee.aleksale.remindly.core.property.RemindlyAppProperties;
import ee.aleksale.remindly.core.service.NtfyHeaderService;
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
    final var type = payload.reminderType();
    final var reminder = property.getReminderProps(type);

    if (!reminder.isEnabled()) {
      throw new RemindlyException("Notification for event type " + type + " is disabled.");
    }

    var headers = ntfyHeaderService.getHeaders(payload);

    final var request = new HttpEntity<>(payload.message(), headers);

    restTemplate.postForEntity(
            property.getNtfyServer() + "/" + reminder.getTopic(),
            request,
            Void.class
    );
  }

  public NtfyRequestBuilder notification(ReminderType reminderType) {
    return new NtfyRequestBuilder(this, reminderType);
  }

  public static class NtfyRequestBuilder {

    private final NtfyClient client;
    private final ReminderType reminderType;

    private String title;
    private String message;
    private List<String> emojis;
    private List<NtfyAction> ntfyActions;

    private NtfyRequestBuilder(NtfyClient client, ReminderType reminderType) {
      this.client = client;
      this.reminderType = reminderType;
    }


    public NtfyRequestBuilder withTitle(String title) {
      this.title = title;
      return this;
    }

    public NtfyRequestBuilder withMessage(String message) {
      this.message = message;
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
              reminderType,
              title,
              message,
              ntfyActions,
              emojis == null ? null : String.join(",", emojis)
      ));
    }
  }
}
