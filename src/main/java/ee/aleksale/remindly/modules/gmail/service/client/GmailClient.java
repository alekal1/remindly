package ee.aleksale.remindly.modules.gmail.service.client;

import static ee.aleksale.remindly.modules.gmail.config.GmailConfiguration.USER_ID;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.auth.oauth2.TokenResponseException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.model.Message;
import ee.aleksale.remindly.core.client.NtfyClient;
import ee.aleksale.remindly.core.model.type.EventType;
import ee.aleksale.remindly.core.model.type.ReminderType;
import ee.aleksale.remindly.modules.gmail.service.GmailOAuthService;
import ee.aleksale.remindly.utils.EmojiUtils;
import jakarta.annotation.Nullable;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class GmailClient {

  private static final String INVALID_GRANT = "invalid_grant";
  private static final String GMAIL_ERROR_TITLE = "GMAIL_ERROR";

  private final Optional<HttpTransport> gmailHttpTransport;
  private final Optional<JsonFactory> gmailJsonFactory;
  private final GmailOAuthService oauthService;
  private final NtfyClient ntfyClient;

  private Optional<Gmail> gmail() throws Exception {
    if (gmailHttpTransport.isEmpty() || gmailJsonFactory.isEmpty()) {
      return Optional.empty();
    }

    Credential credential = oauthService.getCredential();
    if (credential == null) {
      return Optional.empty();
    }

    return Optional.of(
            new Gmail.Builder(
                    gmailHttpTransport.get(),
                    gmailJsonFactory.get(),
                    credential)
                    .setApplicationName("Remindly")
                    .build()
    );
  }

  public List<Message> findMessages(String query) {
    try {
      final var gmail = gmail();
      if (gmail.isEmpty()) {
        log.info("Gmail client is not available.");
        return List.of();
      }

      var response = gmail.get()
              .users()
              .messages()
              .list(USER_ID)
              .setQ(query)
              .execute();

      return response.getMessages() == null
              ? List.of()
              : response.getMessages();
    } catch (TokenResponseException e) {
      handleTokenError(e);
      return List.of();
    } catch (Exception e) {
      log.error("Failed to find messages: {}", e.getMessage());
      notifyError("Failed to find messages: " + e.getMessage());
      return List.of();
    }
  }

  @Nullable
  public Message getMessage(String messageId) {
    try {
      final var gmail = gmail();
      if (gmail.isEmpty()) {
        log.info("Gmail client is not available.");
        return null;
      }

      return gmail.get()
              .users()
              .messages()
              .get(USER_ID, messageId)
              .setFormat("full")
              .execute();
    } catch (TokenResponseException e) {
      handleTokenError(e);
      return null;
    } catch (Exception e) {
      log.error("Failed to get message: {}", e.getMessage());
      notifyError("Failed to get message: " + e.getMessage());
      return null;
    }
  }

  private void handleTokenError(TokenResponseException e) {
    if (e.getDetails() == null || !INVALID_GRANT.equals(e.getDetails().getError())) {
      log.error("Failed to refresh Gmail token: {}", e.getMessage());
      notifyError("Failed to refresh Gmail token: " + e.getMessage());
      return;
    }

    log.warn("Gmail authorization expired or revoked, re-authorization required");
    notifyError("Gmail authorization expired or revoked, re-authorization required");
    try {
      oauthService.clearCredential();
    } catch (IOException ex) {
      log.error("Failed to clear stored Gmail credential: {}", ex.getMessage());
    }
  }

  private void notifyError(String message) {
    try {
      ntfyClient.notification(ReminderType.ERRORS)
              .withTitle(GMAIL_ERROR_TITLE)
              .withMessage(message)
              .withEmojis(EmojiUtils.getEmojisForEventType(EventType.REMINDLY_APP_ERROR))
              .send();
    } catch (Exception e) {
      log.error("Failed to send Gmail error notification: {}", e.getMessage());
    }
  }
}
