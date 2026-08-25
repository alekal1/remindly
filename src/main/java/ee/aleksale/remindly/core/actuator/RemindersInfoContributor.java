package ee.aleksale.remindly.core.actuator;

import ee.aleksale.remindly.core.property.RemindlyAppProperties;
import org.springframework.boot.actuate.info.Info;
import org.springframework.boot.actuate.info.InfoContributor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
public class RemindersInfoContributor implements InfoContributor {

  private final RemindlyAppProperties properties;

  public RemindersInfoContributor(RemindlyAppProperties properties) {
    this.properties = properties;
  }

  @Override
  public void contribute(Info.Builder builder) {
    final var reminders = Optional.ofNullable(properties.getReminders())
            .orElse(List.of())
            .stream()
            .map(reminder -> Map.of(
                    "name", reminder.getName(),
                    "topic", reminder.getTopic(),
                    "enabled", reminder.isEnabled()
            ))
            .toList();

    builder.withDetail("app", Map.of("reminders", reminders));
  }
}
