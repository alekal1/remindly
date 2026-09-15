package ee.aleksale.remindly.modules.gmail.service.processes.unscheduled_garbage;

import ee.aleksale.remindly.modules.garbage_collection.service.GarbageCollectionService;
import ee.aleksale.remindly.modules.gmail.model.GmailContext;
import ee.aleksale.remindly.modules.gmail.service.processes.GmailTemplateProcessor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class UnscheduledGarbageProcessor implements GmailTemplateProcessor {

  private final GarbageCollectionService garbageCollectionService;
  private final UnscheduledGarbageExtractor unscheduledGarbageExtractor;

  @Override
  public String templateId() {
    return "unscheduled_garbage_collection_email_template";
  }

  @Override
  public int process(GmailContext context) {
    final var schedules = unscheduledGarbageExtractor.extractFromTable(context);
    return garbageCollectionService.saveAllGarbageSchedulesNoDuplicate(schedules);
  }
}

