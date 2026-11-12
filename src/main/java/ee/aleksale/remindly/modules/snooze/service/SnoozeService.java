package ee.aleksale.remindly.modules.snooze.service;

import ee.aleksale.remindly.core.client.NtfyClient;
import ee.aleksale.remindly.core.exception.RemindlyException;
import ee.aleksale.remindly.core.model.domain.EventEntity;
import ee.aleksale.remindly.core.model.type.EventType;
import ee.aleksale.remindly.core.model.type.ReminderType;
import ee.aleksale.remindly.core.repository.EventRepository;
import ee.aleksale.remindly.modules.snooze.dto.Snooze;
import ee.aleksale.remindly.utils.Emoji;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SnoozeService {

  private final Clock clock;
  private final EventRepository eventRepository;
  private final NtfyClient ntfyClient;

  @Transactional
  public void snoozeEvent(Snooze request) {
    var parentEvent = eventRepository.findById(request.getParentEventId())
            .orElseThrow(() -> new RemindlyException("No parentEvent found with id: " + request.getParentEventId()));

    var snoozedEvent = new EventEntity();
    snoozedEvent.setScheduledAt(LocalDateTime.now(clock)
            .truncatedTo(ChronoUnit.MINUTES)
            .plusMinutes(request.getSnoozeMinutes()));
    snoozedEvent.setType(EventType.SNOOZE);
    snoozedEvent.setMessage(parentEvent.getMessage());
    snoozedEvent.setParentEvent(parentEvent);

    eventRepository.save(snoozedEvent);

    ntfyClient.notification(ReminderType.ADHOC)
            .withTitle(EventType.SNOOZE.name())
            .withMessage(String.format("Reminder for '%s' is snoozed to %s",
                    parentEvent.getMessage(), snoozedEvent.getScheduledAt()))
            .withEmojis(List.of(Emoji.ZZZ, Emoji.SLEEPING))
            .send();

  }
}
