package ee.aleksale.remindly.modules.garbage_collection.gmail.schedule;

import ee.aleksale.remindly.core.client.NtfyClient;
import ee.aleksale.remindly.core.model.type.EventType;
import ee.aleksale.remindly.modules.garbage_collection.garbage.service.GarbageCollectionService;
import ee.aleksale.remindly.modules.garbage_collection.gmail.property.GmailProperties;
import ee.aleksale.remindly.modules.garbage_collection.gmail.service.GmailHtmlExtractor;
import ee.aleksale.remindly.modules.garbage_collection.gmail.service.client.GmailClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class GmailMessageScheduler {

  private final GmailHtmlExtractor gmailMessageExtractor;
  private final GmailProperties gmailProperties;
  private final GmailClient gmailClient;
  private final GarbageCollectionService garbageCollectionService;
  private final NtfyClient ntfyClient;

  @Scheduled(cron = "0 0 8-18/2 * * *")
  public void processGmailMessages() {
    final var messages = gmailClient.findMessages(String.format("from:%s", gmailProperties.getSender()));

    int savedGarbageSchedules = 0;
    for (var msg : messages) {
      var m = gmailClient.getMessage(msg.getId());
      if (m == null) {
        continue;
      }
      final var garbageSchedules = gmailMessageExtractor.extract(m);
      final var savedSize = garbageCollectionService.saveAllGarbageSchedulesNoDuplicate(garbageSchedules);

      savedGarbageSchedules += savedSize;
    }

    log.info("Found {} unsaved garbage schedules", savedGarbageSchedules);
    if (savedGarbageSchedules > 0) {
      ntfyClient.notification(EventType.GARBAGE_SCHEDULE_FETCHED)
              .withTitle(EventType.GARBAGE_SCHEDULE_FETCHED.name())
              .withDefaultEmojis()
              .withMessage(String.format("Garbage schedule has been fetched. %d entities were added", savedGarbageSchedules))
              .send();
    }
  }
}
