package ee.aleksale.remindly.modules.gmail.service.client;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.Mockito.RETURNS_SELF;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.auth.oauth2.TokenErrorResponse;
import com.google.api.client.auth.oauth2.TokenResponseException;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.testing.http.MockHttpTransport;
import ee.aleksale.remindly.core.client.NtfyClient;
import ee.aleksale.remindly.core.model.type.ReminderType;
import ee.aleksale.remindly.modules.gmail.service.GmailOAuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
class GmailClientTest {

  private GmailOAuthService oauthService;
  private Credential credential;
  private NtfyClient ntfyClient;
  private NtfyClient.NtfyRequestBuilder ntfyRequestBuilder;
  private GmailClient gmailClient;

  @BeforeEach
  void init() {
    oauthService = mock(GmailOAuthService.class);
    credential = mock(Credential.class);
    ntfyClient = mock(NtfyClient.class);
    ntfyRequestBuilder = mock(NtfyClient.NtfyRequestBuilder.class, RETURNS_SELF);
    gmailClient = new GmailClient(
            Optional.of(new MockHttpTransport()),
            Optional.of(GsonFactory.getDefaultInstance()),
            oauthService,
            ntfyClient);

    doReturn(ntfyRequestBuilder).when(ntfyClient).notification(ReminderType.ERRORS);
  }

  @Test
  void shouldClearCredentialAndNotify_whenFindingMessagesAndGrantIsInvalid() throws IOException {
    doReturn(credential).when(oauthService).getCredential();
    doThrow(tokenError("invalid_grant")).when(credential).initialize(any(HttpRequest.class));

    final var result = gmailClient.findMessages("query");

    assertTrue(result.isEmpty());
    verify(oauthService).clearCredential();
    verify(ntfyRequestBuilder).withMessage(contains("re-authorization required"));
    verify(ntfyRequestBuilder).send();
  }

  @Test
  void shouldClearCredentialAndNotify_whenGettingMessageAndGrantIsInvalid() throws IOException {
    doReturn(credential).when(oauthService).getCredential();
    doThrow(tokenError("invalid_grant")).when(credential).initialize(any(HttpRequest.class));

    final var result = gmailClient.getMessage("message-id");

    assertNull(result);
    verify(oauthService).clearCredential();
    verify(ntfyRequestBuilder).send();
  }

  @Test
  void shouldKeepCredentialAndNotify_whenTokenErrorIsNotInvalidGrant() throws IOException {
    doReturn(credential).when(oauthService).getCredential();
    doThrow(tokenError("invalid_client")).when(credential).initialize(any(HttpRequest.class));

    final var result = gmailClient.findMessages("query");

    assertTrue(result.isEmpty());
    verify(oauthService, never()).clearCredential();
    verify(ntfyRequestBuilder).send();
  }

  @Test
  void shouldNotify_whenFindingMessagesFails() throws IOException {
    doReturn(credential).when(oauthService).getCredential();
    doThrow(new IOException("boom")).when(credential).initialize(any(HttpRequest.class));

    final var result = gmailClient.findMessages("query");

    assertTrue(result.isEmpty());
    verify(ntfyRequestBuilder).withMessage("Failed to find messages: boom");
    verify(ntfyRequestBuilder).send();
  }

  @Test
  void shouldNotFail_whenErrorNotificationFails() throws IOException {
    doReturn(credential).when(oauthService).getCredential();
    doThrow(new IOException("boom")).when(credential).initialize(any(HttpRequest.class));
    doThrow(new RuntimeException("ntfy down")).when(ntfyRequestBuilder).send();

    assertNull(gmailClient.getMessage("message-id"));
  }

  private static TokenResponseException tokenError(String error) {
    final var exception = mock(TokenResponseException.class);
    doReturn(new TokenErrorResponse().setError(error)).when(exception).getDetails();
    return exception;
  }
}
