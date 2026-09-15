package ee.aleksale.remindly.core.actuator;

import ee.aleksale.remindly.core.property.RemindlyAppProperties;
import org.junit.jupiter.api.Test;
import org.springframework.boot.actuate.info.Info;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RemindersInfoContributorTest {

  @Test
  void shouldExposeReminderConfigurationInInfoDetails() {
    final var properties = new RemindlyAppProperties();
    properties.setReminders(List.of(
            reminder("errors", "errors-topic", true),
            reminder("adhoc", "adhoc-topic", false)
    ));

    final var contributor = new RemindersInfoContributor(properties);
    final var builder = new Info.Builder();

    contributor.contribute(builder);

    final var details = builder.build().getDetails();
    assertTrue(details.containsKey("app"));

    @SuppressWarnings("unchecked")
    final var appDetails = (Map<String, Object>) details.get("app");
    @SuppressWarnings("unchecked")
    final var reminders = (List<Map<String, Object>>) appDetails.get("reminders");

    assertEquals(2, reminders.size());
    assertEquals("errors", reminders.getFirst().get("id"));
    assertEquals("errors-topic", reminders.getFirst().get("topic"));
    assertEquals(true, reminders.getFirst().get("enabled"));
    assertEquals("adhoc", reminders.get(1).get("id"));
    assertEquals("adhoc-topic", reminders.get(1).get("topic"));
    assertEquals(false, reminders.get(1).get("enabled"));
  }

  private static RemindlyAppProperties.ReminderProperties reminder(String id, String topic, boolean enabled) {
    final var reminder = new RemindlyAppProperties.ReminderProperties();
    reminder.setId(id);
    reminder.setTopic(topic);
    reminder.setEnabled(enabled);
    return reminder;
  }
}
