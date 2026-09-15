package ee.aleksale.remindly.modules.gmail.service;

import ee.aleksale.remindly.modules.gmail.model.GmailContext;
import ee.aleksale.remindly.modules.gmail.service.processes.GmailTemplateProcessor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
public class GmailTemplateProcessingService {

  private final Map<String, GmailTemplateProcessor> processorsByType;

  public GmailTemplateProcessingService(List<GmailTemplateProcessor> processors) {
    this.processorsByType = processors
            .stream()
            .collect(Collectors.toMap(GmailTemplateProcessor::templateId, Function.identity()));
  }

  public int process(GmailContext context) {
    final var templateId = context.template().getId();
    final var processor = processorsByType.get(templateId);

    if (processor == null) {
      log.warn("No processor registered for template id '{}', skipping.", templateId);
      return 0;
    }

    return processor.process(context);
  }
}
