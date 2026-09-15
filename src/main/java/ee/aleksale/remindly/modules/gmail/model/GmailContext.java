package ee.aleksale.remindly.modules.gmail.model;

import com.google.api.services.gmail.model.Message;

public record GmailContext(Message gmailMessage, GmailTemplate template) {}
