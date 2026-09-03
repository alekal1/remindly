package ee.aleksale.remindly.modules.garbage_collection.garbage.service;

import ee.aleksale.remindly.core.client.NtfyClient;
import ee.aleksale.remindly.core.exception.RemindlyException;
import ee.aleksale.remindly.core.model.domain.EventEntity;
import ee.aleksale.remindly.core.model.type.EventType;
import ee.aleksale.remindly.core.repository.EventRepository;
import ee.aleksale.remindly.modules.garbage_collection.garbage.dto.GarbageCollectionSchedule;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GarbageCollectionService {

  private final NtfyClient ntfyClient;
  private final GarbageScheduleExtractorService garbageScheduleExtractorService;
  private final EventRepository eventRepository;

  @Transactional
  public void resetAndExtractSchedules(MultipartFile file) {
    eventRepository.deleteAllByTypeIn(
            Arrays.stream(GarbageCollectionSchedule.GarbageType.values())
                    .map(GarbageCollectionSchedule.GarbageType::mapToEventType)
                    .toList()
    );

    List<GarbageCollectionSchedule> schedule;
    try {
      schedule = garbageScheduleExtractorService.extract(file.getBytes());

      final var savedSize = saveAllGarbageSchedulesNoDuplicate(schedule);

      ntfyClient.notification(EventType.GARBAGE_SCHEDULE_RESET)
              .withTitle(EventType.GARBAGE_SCHEDULE_RESET.name())
              .withDefaultEmojis()
              .withMessage(String.format("Garbage collection schedule has been reset. %d entities were added", savedSize))
              .send();
    } catch (IOException e) {
      throw new RemindlyException(e.getMessage());
    }
  }

  @Transactional
  public int saveAllGarbageSchedulesNoDuplicate(List<GarbageCollectionSchedule> schedules) {
    List<EventEntity> entities = new ArrayList<>();
    for (var scheduleItem : schedules) {
      for (var date : scheduleItem.getDates()) {

        final var alreadyExists = eventRepository.existsByTypeAndScheduledAtBetween(
                scheduleItem.getType().mapToEventType(),
                date.atStartOfDay(),
                date.plusDays(1).atStartOfDay().minusNanos(1)
        );

        if (alreadyExists) {
          continue;
        }

        entities.add(EventEntity.builder()
                .type(scheduleItem.getType().mapToEventType())
                .scheduledAt(LocalDateTime.of(
                        date.getYear(),
                        date.getMonthValue(),
                        date.getDayOfMonth(), 8, 0))
                .sentAt(LocalDateTime.now().toLocalDate().isAfter(date)
                        ? LocalDateTime.now()
                        : null)
                .message(getMessageForGarbageType(scheduleItem.getType()))
                .build());
      }
    }

    eventRepository.saveAll(entities);

    return entities.size();
  }

  private String getMessageForGarbageType(GarbageCollectionSchedule.GarbageType garbageType) {
    return switch (garbageType) {
      case BIO -> "Bio waste collection is scheduled for today.";
      case MIXED -> "Mixed waste collection is scheduled for today.";
      case PACKAGING -> "Packaging waste collection is scheduled for today.";
    };
  }
}
