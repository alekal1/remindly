package ee.aleksale.remindly.modules.snooze.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import ee.aleksale.remindly.core.client.NtfyClient;
import ee.aleksale.remindly.core.exception.RemindlyException;
import ee.aleksale.remindly.core.model.domain.EventEntity;
import ee.aleksale.remindly.core.model.type.EventType;
import ee.aleksale.remindly.core.model.type.ReminderType;
import ee.aleksale.remindly.core.repository.EventRepository;
import ee.aleksale.remindly.modules.snooze.dto.Snooze;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
public class SnoozeServiceTest {

  // "Now" is 10:30:45, i.e. the user taps snooze long after the 08:00 reminder fired
  private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-08-25T10:30:45Z"), ZoneOffset.UTC);

  private NtfyClient ntfyClient;
  private NtfyClient.NtfyRequestBuilder ntfyRequestBuilder;
  private EventRepository eventRepository;

  private SnoozeService snoozeService;

  private ArgumentCaptor<EventEntity> eventCaptor;

  @BeforeEach
  void init() {
    ntfyClient = mock(NtfyClient.class);
    ntfyRequestBuilder = mock(NtfyClient.NtfyRequestBuilder.class);
    eventRepository = mock(EventRepository.class);

    snoozeService = new SnoozeService(CLOCK, eventRepository, ntfyClient);

    eventCaptor = ArgumentCaptor.forClass(EventEntity.class);
  }

  @Test
  void shouldScheduleSnoozeRelativeToNow_notToParentScheduledAt() {
    final var parentEvent = EventEntity.builder()
        .id(1L)
        .type(EventType.BIO_WASTE_COLLECTION)
        .message("parent message")
        .scheduledAt(LocalDateTime.of(2026, 8, 25, 8, 0))
        .build();
    final var request = Snooze.builder()
        .parentEventId(1L)
        .snoozeMinutes(60)
        .build();
    final var expectedScheduledAt = LocalDateTime.of(2026, 8, 25, 11, 30);

    doReturn(Optional.of(parentEvent)).when(eventRepository).findById(1L);
    doReturn(ntfyRequestBuilder).when(ntfyClient).notification(any(ReminderType.class));
    doReturn(ntfyRequestBuilder).when(ntfyRequestBuilder).withTitle(anyString());
    doReturn(ntfyRequestBuilder).when(ntfyRequestBuilder).withMessage(anyString());
    doReturn(ntfyRequestBuilder).when(ntfyRequestBuilder).withEmojis(any());

    snoozeService.snoozeEvent(request);

    verify(eventRepository).save(eventCaptor.capture());
    verify(ntfyClient).notification(ReminderType.ADHOC);
    verify(ntfyRequestBuilder).withTitle(EventType.SNOOZE.name());
    verify(ntfyRequestBuilder).withMessage(
        String.format("Reminder for '%s' is snoozed to %s", "parent message", expectedScheduledAt));
    verify(ntfyRequestBuilder).withEmojis(any());

    final var savedEntity = eventCaptor.getValue();
    assertEquals(EventType.SNOOZE, savedEntity.getType());
    assertEquals("parent message", savedEntity.getMessage());
    assertEquals(expectedScheduledAt, savedEntity.getScheduledAt());
    assertEquals(parentEvent, savedEntity.getParentEvent());
  }

  @Test
  void shouldThrowRemindlyException_whenParentEventNotFound() {
    final var request = Snooze.builder()
        .parentEventId(99L)
        .snoozeMinutes(60)
        .build();

    doReturn(Optional.empty()).when(eventRepository).findById(99L);

    final var exception = assertThrows(RemindlyException.class, () -> snoozeService.snoozeEvent(request));

    assertEquals("No parentEvent found with id: 99", exception.getMessage());
    verify(eventRepository, never()).save(any());
  }
}
