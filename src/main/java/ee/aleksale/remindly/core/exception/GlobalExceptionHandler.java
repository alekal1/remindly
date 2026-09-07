package ee.aleksale.remindly.core.exception;

import ee.aleksale.remindly.core.client.NtfyClient;
import ee.aleksale.remindly.core.model.type.EventType;
import ee.aleksale.remindly.core.model.type.ReminderType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@Slf4j
@ControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {

  private final NtfyClient ntfyClient;

  @ExceptionHandler(NoResourceFoundException.class)
  @ResponseStatus(HttpStatus.NOT_FOUND)
  public void handleNoResourceFoundException(NoResourceFoundException exception) {
    log.debug("No static resource found for request '{}'", exception.getResourcePath());
  }

  @ExceptionHandler(Exception.class)
  public void handleException(Exception exception) {
    log.error("Unhandled exception during request processing", exception);

    ntfyClient.notification(EventType.REMINDLY_APP_ERROR)
            .withTitle(ReminderType.ERRORS.name())
            .withMessage(exception.getMessage())
            .withDefaultEmojis()
            .send();
  }
}
