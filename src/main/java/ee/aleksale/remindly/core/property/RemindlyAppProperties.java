package ee.aleksale.remindly.core.property;

import ee.aleksale.remindly.core.exception.RemindlyException;
import ee.aleksale.remindly.core.model.type.ReminderType;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@Getter
@Setter
@ConfigurationProperties(prefix = "app")
public class RemindlyAppProperties {

    private String externalBaseUrl;
    private String ntfyServer;
    private List<ReminderProperties> reminders;

    public ReminderProperties getReminders(ReminderType reminderType) {
        final var reminder = reminders.stream()
                .filter(n -> n.getName().equals(reminderType.getConfigKey()))
                .findFirst();

        if (reminder.isEmpty()) {
            throw new RemindlyException("Notification configuration not found for type: " + reminderType);
        }
        return reminder.get();
    }

    @Getter
    @Setter
    public static class ReminderProperties {
        private String name;
        private String topic;
        private boolean enabled;
    }

}
