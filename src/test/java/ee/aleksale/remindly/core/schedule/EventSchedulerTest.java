package ee.aleksale.remindly.core.schedule;

import ee.aleksale.remindly.core.client.NtfyClient;
import ee.aleksale.remindly.core.property.RemindlyAppProperties;
import ee.aleksale.remindly.core.model.domain.EventEntity;
import ee.aleksale.remindly.core.model.type.EventType;
import ee.aleksale.remindly.core.repository.EventRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import tools.jackson.databind.ObjectMapper;

@ExtendWith(MockitoExtension.class)
class EventSchedulerTest {

  private EventRepository eventRepository;
  private NtfyClient ntfyClient;
  private NtfyClient.NtfyRequestBuilder ntfyRequestBuilder;
  private RemindlyAppProperties properties;
  private ObjectMapper objectMapper;

  @BeforeEach()
  void init() {
    eventRepository = mock(EventRepository.class);
    ntfyClient = mock(NtfyClient.class);
    ntfyRequestBuilder = mock(NtfyClient.NtfyRequestBuilder.class);
    properties = mock(RemindlyAppProperties.class);
    objectMapper = new ObjectMapper();
  }

  @Test
  void shouldSendDueEventOnlyOnce_whenProcessEventsRunsTwice_withSameDueEvent() {
    final var clock = Clock.fixed(Instant.parse("2026-08-25T07:00:30Z"), ZoneOffset.UTC);
    final var dueAt = LocalDateTime.of(2026, 8, 25, 7, 0);
    final var event = EventEntity.builder()
            .id(1L)
            .type(EventType.ADHOC)
            .scheduledAt(dueAt.minusMinutes(1))
            .message("test message")
            .build();

    doReturn(List.of(event), List.of())
            .when(eventRepository)
            .findAllBySentAtIsNullAndScheduledAtLessThanEqual(dueAt);
    doReturn(ntfyRequestBuilder).when(ntfyClient).notification(any(EventType.class));
    doReturn(ntfyRequestBuilder).when(ntfyRequestBuilder).withTitle(anyString());
    doReturn(ntfyRequestBuilder).when(ntfyRequestBuilder).withMessage(anyString());
    doReturn(ntfyRequestBuilder).when(ntfyRequestBuilder).withDefaultEmojis();
    doReturn(ntfyRequestBuilder).when(ntfyRequestBuilder).withActions(anyList());
    doReturn("https://example.com").when(properties).getExternalBaseUrl();

    final var scheduler = new EventScheduler(clock, eventRepository, ntfyClient, properties, objectMapper);

    scheduler.processEvents();
    scheduler.processEvents();

    verify(ntfyRequestBuilder, times(1)).send();
    verify(eventRepository, times(1)).save(event);
    assertThat(event.getSentAt()).isEqualTo(dueAt);
  }
}
