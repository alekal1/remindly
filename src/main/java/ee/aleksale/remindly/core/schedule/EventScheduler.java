package ee.aleksale.remindly.core.schedule;

import ee.aleksale.remindly.core.client.NtfyClient;
import ee.aleksale.remindly.core.constants.ApiConstants;
import ee.aleksale.remindly.core.model.domain.EventEntity;
import ee.aleksale.remindly.core.model.dto.NtfyAction;
import ee.aleksale.remindly.core.model.type.DeliveryChannel;
import ee.aleksale.remindly.core.model.type.EventType;
import ee.aleksale.remindly.core.property.RemindlyAppProperties;
import ee.aleksale.remindly.core.repository.EventRepository;
import ee.aleksale.remindly.modules.calendar.property.CalendarProperties;
import ee.aleksale.remindly.modules.snooze.dto.Snooze;
import ee.aleksale.remindly.utils.EmojiUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class EventScheduler {

  private final Clock clock;
  private final EventRepository eventRepository;
  private final NtfyClient ntfyClient;
  private final RemindlyAppProperties properties;
  private final ObjectMapper objectMapper;
  private final CalendarProperties calendarProperties;

  @Transactional
  @Scheduled(cron = "*/30 * * * * *")
  public void processEvents() {
    final var now = LocalDateTime.now(clock).truncatedTo(ChronoUnit.MINUTES);

    final var dueEvents = eventRepository.findAllBySentAtIsNullAndScheduledAtLessThanEqualAndTypeIn(now, ntfyEventTypes());

    for (var event : dueEvents) {
      log.info("Processing event: type :{}; scheduled at: {}", event.getType(), event.getScheduledAt());

      ntfyClient.notification(event.getType().getReminderType())
              .withTitle(event.getType().getReminderType().name())
              .withMessage(event.getMessage())
              .withEmojis(EmojiUtils.getEmojisForEventType(event.getType()))
              .withActions(actions(event))
              .send();

      event.setSentAt(now);
      eventRepository.save(event);
    }
  }

  private List<EventType> ntfyEventTypes() {
    final var types = new ArrayList<>(EventType.byChannel(DeliveryChannel.NTFY));
    if (calendarProperties.isNtfyBackupEnabled()) {
      types.addAll(EventType.byChannel(DeliveryChannel.CALENDAR));
    }
    return types;
  }

  private List<NtfyAction> actions(EventEntity event) {
    return List.of(
            NtfyAction.builder()
                    .action(NtfyAction.NtfyActionType.HTTP)
                    .label("Snooze (1 hour)")
                    .url(properties.getExternalBaseUrl() + ApiConstants.SNOOZE_API_URL)
                    .body(snooze(event.getId(), 60))
                    .headers(Map.of(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE))
                    .build(),
            NtfyAction.builder()
                    .action(NtfyAction.NtfyActionType.HTTP)
                    .label("Snooze (3 hours)")
                    .url(properties.getExternalBaseUrl() + ApiConstants.SNOOZE_API_URL)
                    .body(snooze(event.getId(), 180))
                    .headers(Map.of(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE))
                    .build());
  }

  private String snooze(Long eventId, int snoozeMinutes) {
    try {
      return objectMapper.writeValueAsString(
              Snooze.builder()
                      .parentEventId(eventId)
                      .snoozeMinutes(snoozeMinutes)
                      .build()
      );
    } catch (Exception e) {
      throw new RuntimeException("Failed to serialize Snooze request", e);
    }
  }
}
