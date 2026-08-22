package ee.aleksale.remindly.garbage_collection.service;

import ee.aleksale.remindly.core.client.NtfyClient;
import ee.aleksale.remindly.core.exception.RemindlyException;
import ee.aleksale.remindly.core.model.domain.EventEntity;
import ee.aleksale.remindly.core.model.type.EventType;
import ee.aleksale.remindly.core.repository.EventRepository;
import ee.aleksale.remindly.garbage_collection.dto.GarbageCollectionSchedule;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class GarbageCollectionService {

  private final NtfyClient ntfyClient;
  private final GarbageScheduleExtractorService garbageScheduleExtractorService;
  private final EventRepository eventRepository;

  private final Set<EventType> eventTypes = Set.of(
      EventType.BIO_WASTE_COLLECTION,
      EventType.MIXED_WASTE_COLLECTION,
      EventType.PACKAGING_WASTE_COLLECTION
  );

  @Transactional
  public void resetAndExtractSchedules(MultipartFile file) {
    eventRepository.deleteAllByTypeIn(eventTypes);

    List<GarbageCollectionSchedule> schedule;
    try {
      schedule = garbageScheduleExtractorService.extract(file.getBytes());

      List<EventEntity> entities = new ArrayList<>();
      for (var scheduleItem : schedule) {
        for (var date : scheduleItem.getDates()) {
          entities.add(EventEntity.builder()
                  .type(scheduleItem.getType().mapToEventType())
                  .scheduledAt(LocalDateTime.of(
                                  date.getYear(), date.getMonthValue(), date.getDayOfMonth(), 15, 0)
                          .minusDays(1))
                  .build());
        }
      }

      eventRepository.saveAll(entities);

      ntfyClient.sendNotification(
              EventType.GARBAGE_SCHEDULE_RESET,
              String.format("Garbage collection schedule has been reset. %d entities were added", entities.size()));
    } catch (IOException e) {
      throw new RemindlyException(e.getMessage());
    }
  }
}
