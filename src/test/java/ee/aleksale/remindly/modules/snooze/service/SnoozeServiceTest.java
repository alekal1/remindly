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
import ee.aleksale.remindly.core.repository.EventRepository;
import ee.aleksale.remindly.modules.snooze.dto.Snooze;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
public class SnoozeServiceTest {

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

    snoozeService = new SnoozeService(eventRepository, ntfyClient);

    eventCaptor = ArgumentCaptor.forClass(EventEntity.class);
  }

  @Test
  void shouldSaveSnoozedEventAndNotify_whenParentEventFound() {
    final var parentEvent = EventEntity.builder()
        .id(1L)
        .type(EventType.ADHOC)
        .message("parent message")
        .scheduledAt(LocalDateTime.of(2026, 8, 25, 10, 0))
        .build();
    final var newScheduledAt = LocalDateTime.of(2026, 8, 26, 10, 0);
    final var request = Snooze.builder()
        .parentEventId(1L)
        .newScheduledAt(newScheduledAt)
        .build();

    doReturn(Optional.of(parentEvent)).when(eventRepository).findById(1L);
    doReturn(ntfyRequestBuilder).when(ntfyClient).notification(any(EventType.class));
    doReturn(ntfyRequestBuilder).when(ntfyRequestBuilder).withTitle(anyString());
    doReturn(ntfyRequestBuilder).when(ntfyRequestBuilder).withMessage(anyString());
    doReturn(ntfyRequestBuilder).when(ntfyRequestBuilder).withEmojis(any());

    snoozeService.snoozeEvent(request);

    verify(eventRepository).save(eventCaptor.capture());
    verify(ntfyClient).notification(EventType.SNOOZE);
    verify(ntfyRequestBuilder).withTitle(EventType.SNOOZE.name());
    verify(ntfyRequestBuilder).withMessage(
        String.format("Reminder for '%s' is snoozed to %s", "parent message", newScheduledAt));
    verify(ntfyRequestBuilder).withEmojis(any());

    final var savedEntity = eventCaptor.getValue();
    assertEquals(EventType.SNOOZE, savedEntity.getType());
    assertEquals("parent message", savedEntity.getMessage());
    assertEquals(newScheduledAt, savedEntity.getScheduledAt());
    assertEquals(parentEvent, savedEntity.getParentEvent());
  }

  @Test
  void shouldThrowRemindlyException_whenParentEventNotFound() {
    final var request = Snooze.builder()
        .parentEventId(99L)
        .newScheduledAt(LocalDateTime.now())
        .build();

    doReturn(Optional.empty()).when(eventRepository).findById(99L);

    final var exception = assertThrows(RemindlyException.class, () -> snoozeService.snoozeEvent(request));

    assertEquals("No parentEvent found with id: 99", exception.getMessage());
    verify(eventRepository, never()).save(any());
  }
}
