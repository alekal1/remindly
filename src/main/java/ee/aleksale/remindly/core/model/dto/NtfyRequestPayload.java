package ee.aleksale.remindly.core.model.dto;

import ee.aleksale.remindly.core.model.type.ReminderType;

import java.util.List;

public record NtfyRequestPayload(
        ReminderType reminderType,
        String title,
        String message,
        List<NtfyAction> ntfyActions,
        String emojis) {}
