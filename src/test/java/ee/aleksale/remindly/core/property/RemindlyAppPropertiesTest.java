package ee.aleksale.remindly.core.property;

import ee.aleksale.remindly.core.exception.RemindlyException;
import ee.aleksale.remindly.core.model.type.ReminderType;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RemindlyAppPropertiesTest {

  @Test
  void shouldReturnReminderConfigurationForType() {
    final var properties = new RemindlyAppProperties();
    final var adhocReminder = reminder("adhoc", "adhoc-topic", true);
    properties.setReminders(List.of(adhocReminder));

    final var reminder = properties.getReminders(ReminderType.ADHOC);

    assertEquals("adhoc", reminder.getName());
    assertEquals("adhoc-topic", reminder.getTopic());
    assertTrue(reminder.isEnabled());
  }

  @Test
  void shouldThrow_whenReminderConfigurationIsMissing() {
    final var properties = new RemindlyAppProperties();
    properties.setReminders(List.of(reminder("adhoc", "adhoc-topic", true)));

    final var exception = assertThrows(
            RemindlyException.class,
            () -> properties.getReminders(ReminderType.ERRORS)
    );

    assertEquals("Notification configuration not found for type: ERRORS", exception.getMessage());
  }

  private static RemindlyAppProperties.ReminderProperties reminder(String name, String topic, boolean enabled) {
    final var reminder = new RemindlyAppProperties.ReminderProperties();
    reminder.setName(name);
    reminder.setTopic(topic);
    reminder.setEnabled(enabled);
    return reminder;
  }
}
