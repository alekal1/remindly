package ee.aleksale.remindly.modules.calendar.property;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@Getter
@Setter
@ConfigurationProperties(prefix = "app.calendar")
public class CalendarProperties {

  private String feedToken;
  private Duration alarmTrigger = Duration.ofHours(8);
  private int pastDays = 30;
}
