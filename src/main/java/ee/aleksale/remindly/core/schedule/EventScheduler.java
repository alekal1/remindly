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
import java.time.temporal.ChronoUnit;

@Slf4j
@Component
@EnableScheduling
@RequiredArgsConstructor
public class EventScheduler {

  private final Clock clock;
  private final EventRepository eventRepository;
  private final NtfyClient ntfyClient;

  @Scheduled(cron = "0 * * * * *")
  public void processEvents() {
    log.info("Running event scheduler.");
    final var now = LocalDateTime.now(clock).truncatedTo(ChronoUnit.MINUTES);

    final var dueEvents = eventRepository.findAllBySentAtIsNullAndScheduledAtLessThanEqual(now);
    log.info("Found {} due events", dueEvents.size());

    for (var event : dueEvents) {
      ntfyClient.sendNotification(event.getType(), event.getType().getNotificationMessage());
      event.setSentAt(now);
      eventRepository.save(event);
    }
  }
}
