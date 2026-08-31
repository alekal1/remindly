package ee.aleksale.remindly.core.model.dto;

import ee.aleksale.remindly.core.model.type.EventType;

import java.util.List;

public record NtfyRequestPayload(
        EventType eventType,
        String title,
        String message,
        List<NtfyAction> ntfyActions,
        String emojis) {}
