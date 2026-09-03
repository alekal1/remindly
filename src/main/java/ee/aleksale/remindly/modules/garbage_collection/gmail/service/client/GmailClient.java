package ee.aleksale.remindly.modules.garbage_collection.gmail.service.client;

import static ee.aleksale.remindly.modules.garbage_collection.gmail.config.GmailConfiguration.USER_ID;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.model.Message;
import ee.aleksale.remindly.modules.garbage_collection.gmail.service.GmailOAuthService;
import jakarta.annotation.Nullable;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class GmailClient {

  private final Optional<HttpTransport> gmailHttpTransport;
  private final Optional<JsonFactory> gmailJsonFactory;
  private final GmailOAuthService oauthService;

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
    } catch (Exception e) {
      log.error("Failed to find messages: {}", e.getMessage());
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
    } catch (Exception e) {
      log.error("Failed to get message: {}", e.getMessage());
      return null;
    }
  }
}
