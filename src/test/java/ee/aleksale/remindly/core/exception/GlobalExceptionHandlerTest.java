package ee.aleksale.remindly.core.exception;

import ee.aleksale.remindly.core.client.NtfyClient;
import ee.aleksale.remindly.core.model.type.EventType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {

  private NtfyClient ntfyClient;
  private NtfyClient.NtfyRequestBuilder ntfyRequestBuilder;
  private GlobalExceptionHandler globalExceptionHandler;

  @BeforeEach
  void init() {
    ntfyClient = mock(NtfyClient.class);
    ntfyRequestBuilder = mock(NtfyClient.NtfyRequestBuilder.class);
    globalExceptionHandler = new GlobalExceptionHandler(ntfyClient);
  }

  @Test
  void shouldSendErrorNotification_whenExceptionIsHandled() {
    final var exception = new RuntimeException("boom");

    doReturn(ntfyRequestBuilder).when(ntfyClient).notification(EventType.REMINDLY_APP_ERROR);
    doReturn(ntfyRequestBuilder).when(ntfyRequestBuilder).withMessage(anyString());
    doReturn(ntfyRequestBuilder).when(ntfyRequestBuilder).withDefaultEmojis();

    globalExceptionHandler.handleException(exception);

    verify(ntfyClient).notification(EventType.REMINDLY_APP_ERROR);
    verify(ntfyRequestBuilder).withMessage("boom");
    verify(ntfyRequestBuilder).withDefaultEmojis();
    verify(ntfyRequestBuilder).send();
  }
}
