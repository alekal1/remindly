package ee.aleksale.remindly.core.schedule;

import ee.aleksale.remindly.core.client.NtfyClient;
import ee.aleksale.remindly.core.constants.ApiConstants;
import ee.aleksale.remindly.core.model.domain.EventEntity;
import ee.aleksale.remindly.core.model.dto.NtfyAction;
import ee.aleksale.remindly.core.property.RemindlyAppProperties;
import ee.aleksale.remindly.core.repository.EventRepository;
import ee.aleksale.remindly.modules.snooze.dto.Snooze;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@EnableScheduling
@RequiredArgsConstructor
public class EventScheduler {

  private final Clock clock;
  private final EventRepository eventRepository;
  private final NtfyClient ntfyClient;
  private final RemindlyAppProperties properties;
  private final ObjectMapper objectMapper;

  @Transactional
  @Scheduled(cron = "*/30 * * * * *")
  public void processEvents() {
    final var now = LocalDateTime.now(clock).truncatedTo(ChronoUnit.MINUTES);

    final var dueEvents = eventRepository.findAllBySentAtIsNullAndScheduledAtLessThanEqual(now);
    log.info("Found {} due events", dueEvents.size());

    for (var event : dueEvents) {
      ntfyClient.notification(event.getType())
              .withTitle(event.getType().getReminderType().name())
              .withMessage(event.getMessage())
              .withDefaultEmojis()
              .withActions(actions(event))
              .send();

      event.setSentAt(now);
      eventRepository.save(event);
    }
  }

  private List<NtfyAction> actions(EventEntity event) {
    return List.of(
            NtfyAction.builder()
                    .action(NtfyAction.NtfyActionType.HTTP)
                    .label("Snooze (1 hour)")
                    .url(properties.getExternalBaseUrl() + ApiConstants.SNOOZE_API_URL)
                    .body(snooze(event.getId(), event.getScheduledAt().plusHours(1)))
                    .headers(Map.of(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE))
                    .build(),
            NtfyAction.builder()
                    .action(NtfyAction.NtfyActionType.HTTP)
                    .label("Snooze (3 hours)")
                    .url(properties.getExternalBaseUrl() + ApiConstants.SNOOZE_API_URL)
                    .body(snooze(event.getId(), event.getScheduledAt().plusHours(3)))
                    .headers(Map.of(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE))
                    .build());
  }

  private String snooze(Long eventId, LocalDateTime time) {
    try {
      return objectMapper.writeValueAsString(
              Snooze.builder()
                      .parentEventId(eventId)
                      .newScheduledAt(time)
                      .build()
      );
    } catch (Exception e) {
      throw new RuntimeException("Failed to serialize Snooze request", e);
    }
  }
}
