package ee.aleksale.remindly.modules.adhoc.dto;

import jakarta.validation.constraints.NotBlank;
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
public class Adhoc {

  @NotBlank(message = "'message' is required")
  private String message;

  @NotNull(message = "'scheduledAt' is required")
  private LocalDateTime scheduledAt;
}
