package ee.aleksale.remindly.core.client;

import ee.aleksale.remindly.core.exception.RemindlyException;
import ee.aleksale.remindly.core.model.dto.NtfyAction;
import ee.aleksale.remindly.core.model.type.EventType;
import ee.aleksale.remindly.core.model.type.ReminderType;
import ee.aleksale.remindly.core.property.RemindlyAppProperties;
import ee.aleksale.remindly.core.service.NtfyHeaderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class NtfyClientTest {

  private RestTemplate restTemplate;
  private RemindlyAppProperties properties;
  private NtfyHeaderService ntfyHeaderService;
  private NtfyClient ntfyClient;

  @BeforeEach
  void init() {
    restTemplate = mock(RestTemplate.class);
    properties = mock(RemindlyAppProperties.class);
    ntfyHeaderService = new NtfyHeaderService();
    ntfyClient = new NtfyClient(restTemplate, properties, ntfyHeaderService);
  }

  @Test
  void shouldSendNotification_withExpectedUrlHeadersAndBody() {
    final var reminder = reminder("adhoc", "topic-adhoc", true);
    final var urlCaptor = ArgumentCaptor.forClass(String.class);
    final var requestCaptor = ArgumentCaptor.forClass(HttpEntity.class);

    doReturn("http://ntfy.sh").when(properties).getNtfyServer();
    doReturn(reminder).when(properties).getReminders(ReminderType.ADHOC);
    doReturn(ResponseEntity.ok().build())
            .when(restTemplate)
            .postForEntity(anyString(), any(HttpEntity.class), any(Class.class));

    ntfyClient.notification(EventType.ADHOC)
            .withTitle("ADHOC")
            .withMessage("test message")
            .withEmojis(List.of("memo", "bell"))
            .send();

    verify(restTemplate).postForEntity(urlCaptor.capture(), requestCaptor.capture(), any(Class.class));

    assertEquals("http://ntfy.sh/topic-adhoc", urlCaptor.getValue());
    assertEquals("test message", requestCaptor.getValue().getBody());
    assertEquals("memo,bell", requestCaptor.getValue().getHeaders().getFirst("Tags"));
    assertEquals("ADHOC", requestCaptor.getValue().getHeaders().getFirst("Title"));
  }

   @Test
   void shouldQuoteHttpActionBody_whenBodyContainsJson() {
     final var reminder = reminder("adhoc", "topic-adhoc", true);
     final var requestCaptor = ArgumentCaptor.forClass(HttpEntity.class);

     doReturn("http://ntfy.sh").when(properties).getNtfyServer();
     doReturn(reminder).when(properties).getReminders(ReminderType.ADHOC);
     doReturn(ResponseEntity.ok().build())
             .when(restTemplate)
             .postForEntity(anyString(), any(HttpEntity.class), any(Class.class));

     ntfyClient.notification(EventType.ADHOC)
             .withMessage("test message")
             .withAction(NtfyAction.builder()
                     .action(NtfyAction.NtfyActionType.HTTP)
                     .label("Snooze")
                     .url("https://example.com/v1/snooze")
                     .method(HttpMethod.POST)
                     .body("{\"eventId\":1,\"newScheduledAt\":\"2026-08-25T08:00:00\"}")
                     .build())
             .send();

     verify(restTemplate).postForEntity(anyString(), requestCaptor.capture(), any(Class.class));

     assertEquals(
             "http, Snooze, https://example.com/v1/snooze, method=POST, body='{\"eventId\":1,\"newScheduledAt\":\"2026-08-25T08:00:00\"}'",
             requestCaptor.getValue().getHeaders().getFirst("Actions")
     );
   }

  @Test
  void shouldThrow_whenReminderIsDisabled() {
    final var reminder = reminder("adhoc", "topic-adhoc", false);
    doReturn(reminder).when(properties).getReminders(ReminderType.ADHOC);

    final var exception = assertThrows(
            RemindlyException.class,
            () -> ntfyClient.notification(EventType.ADHOC).withMessage("test").withEmojis(List.of("memo")).send()
    );

    assertEquals("Notification for event type ADHOC is disabled.", exception.getMessage());
    verify(restTemplate, never()).postForEntity(anyString(), any(HttpEntity.class), any(Class.class));
  }

  private static RemindlyAppProperties.ReminderProperties reminder(String name, String topic, boolean enabled) {
    final var reminder = new RemindlyAppProperties.ReminderProperties();
    reminder.setName(name);
    reminder.setTopic(topic);
    reminder.setEnabled(enabled);
    return reminder;
  }
}
