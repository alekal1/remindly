package ee.aleksale.remindly.modules.snooze.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Snooze {

  @NotNull(message = "'parentEventId' is required")
  private Long parentEventId;
  @NotNull(message = "'snoozeMinutes' is required")
  @Positive(message = "'snoozeMinutes' must be positive")
  private Integer snoozeMinutes;
}
