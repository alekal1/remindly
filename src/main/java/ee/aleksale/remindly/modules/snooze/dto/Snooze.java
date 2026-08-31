package ee.aleksale.remindly.modules.snooze.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Snooze {

  @NotNull(message = "'parentEventId' is required")
  private Long parentEventId;
  @NotNull(message = "'newScheduledAt' is required")
  private LocalDateTime newScheduledAt;
}
