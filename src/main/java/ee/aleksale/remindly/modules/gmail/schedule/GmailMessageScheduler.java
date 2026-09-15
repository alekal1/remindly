package ee.aleksale.remindly.modules.gmail.schedule;

import ee.aleksale.remindly.core.client.NtfyClient;
import ee.aleksale.remindly.core.model.type.ReminderType;
import ee.aleksale.remindly.modules.gmail.model.GmailTemplate;
import ee.aleksale.remindly.modules.gmail.model.GmailTemplateLoader;
import ee.aleksale.remindly.modules.gmail.model.GmailContext;
import ee.aleksale.remindly.modules.gmail.service.GmailTemplateProcessingService;
import ee.aleksale.remindly.modules.gmail.service.client.GmailClient;
import ee.aleksale.remindly.utils.EmojiUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class GmailMessageScheduler {

  private final NtfyClient ntfyClient;
  private final GmailTemplateProcessingService gmailTemplateProcessingService;
  private final GmailTemplateLoader gmailTemplateLoader;
  private final GmailClient gmailClient;

  @Scheduled(cron = "0 0 8-18/2 * * *")
  public void processGmailMessages() {
    Map<GmailTemplate, Integer> savedEntitiesByTemplate = new HashMap<>();

    for (var template : gmailTemplateLoader.getTemplates()) {
      final var messages = gmailClient.findMessages(template.getGmailQuery());

      for (var msg : messages) {
        var m = gmailClient.getMessage(msg.getId());
        if (m == null) {
          continue;
        }

        var processedObjects = gmailTemplateProcessingService.process(new GmailContext(m, template));
        if (processedObjects > 0) {
          savedEntitiesByTemplate.put(template, savedEntitiesByTemplate.getOrDefault(template, 0) + processedObjects);
        }
      }
    }

    if (savedEntitiesByTemplate.isEmpty()) {
      return;
    }

    sendNotifications(savedEntitiesByTemplate);
  }


  private void sendNotifications(Map<GmailTemplate, Integer> savedEntitiesByTemplate) {

    for (final var entry : savedEntitiesByTemplate.entrySet()) {
        final var template = entry.getKey();
        final var savedEntities = entry.getValue();

        final var reminderType = ReminderType.fromId(template.getReminderId());

        ntfyClient.notification(reminderType)
                .withTitle("Gmail fetched for template " + template.getId())
                .withEmojis(List.of(EmojiUtils.INBOX_TRAY, EmojiUtils.EMAIL))
                .withMessage(String.format("%d entities were processed.", savedEntities))
                .send();
    }
  }
}
