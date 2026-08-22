package ee.aleksale.remindly.core.schedule;

import ee.aleksale.remindly.core.client.NtfyClient;
import ee.aleksale.remindly.core.repository.EventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.LocalDateTime;

@Slf4j
@Component
@EnableScheduling
@RequiredArgsConstructor
public class EventScheduler {

  private final Clock clock;
  private final EventRepository eventRepository;
  private final NtfyClient ntfyClient;

  @Scheduled(cron = "0 0 * * * *")
  public void processEvents() {
    final var now = LocalDateTime.now(clock);

    final var dueEvents = eventRepository.findAllBySentAtIsNullAndScheduledAtLessThanEqual(now);

    for (var event : dueEvents) {
      ntfyClient.sendNotification(event.getType(), event.getType().getNotificationMessage());
      event.setSentAt(now);
      eventRepository.save(event);
    }
  }
}
