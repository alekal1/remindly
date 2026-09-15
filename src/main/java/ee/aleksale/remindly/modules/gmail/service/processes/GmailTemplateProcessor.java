package ee.aleksale.remindly.modules.gmail.service.processes;

import ee.aleksale.remindly.modules.gmail.model.GmailContext;

public interface GmailTemplateProcessor {

  String templateId();

  int process(GmailContext context);
}
