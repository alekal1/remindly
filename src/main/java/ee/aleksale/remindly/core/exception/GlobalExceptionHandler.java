package ee.aleksale.remindly.core.exception;

import ee.aleksale.remindly.core.client.NtfyClient;
import ee.aleksale.remindly.core.model.type.EventType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@Slf4j
@ControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {

  private final NtfyClient ntfyClient;

  @ExceptionHandler(Exception.class)
  public void handleException(Exception exception) {
    log.error("Unhandled exception during request processing", exception);

    ntfyClient.sendNotification(EventType.REMINDLY_APP_ERROR, exception.getMessage());
  }
}
