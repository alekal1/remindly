package ee.aleksale.remindly.core.aspect;

import ee.aleksale.remindly.core.annotation.ReminderEnabled;
import ee.aleksale.remindly.core.exception.RemindlyException;
import ee.aleksale.remindly.core.property.RemindlyAppProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class ReminderEnabledAspect {

    private final RemindlyAppProperties properties;

    @Before("@annotation(reminderEnabled)")
    public void checkReminderEnabled(ReminderEnabled reminderEnabled) {
        log.info("Checking reminder enabled: {}", reminderEnabled);

        final var reminder = properties.getReminders(reminderEnabled.type());

        if (!reminder.isEnabled()) {
            throw new RemindlyException("Reminder '" + reminderEnabled.type() + "' is disabled.");
        }
    }
}
