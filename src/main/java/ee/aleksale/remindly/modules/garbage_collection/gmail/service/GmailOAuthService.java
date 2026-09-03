package ee.aleksale.remindly.modules.garbage_collection.gmail.service;

import static ee.aleksale.remindly.modules.garbage_collection.gmail.config.GmailConfiguration.USER_ID;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import ee.aleksale.remindly.core.constants.ApiConstants;
import ee.aleksale.remindly.core.property.RemindlyAppProperties;
import ee.aleksale.remindly.modules.garbage_collection.gmail.property.GmailProperties;
import jakarta.annotation.Nullable;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class GmailOAuthService {

  private static final String OFFLINE_ACCESS = "offline";

  private final Optional<GoogleAuthorizationCodeFlow> gmailFlow;
  private final GmailProperties properties;

  @Nullable
  public String authorizationUrl() {
    return gmailFlow.map(googleAuthorizationCodeFlow -> googleAuthorizationCodeFlow.newAuthorizationUrl()
            .setRedirectUri(callbackUrl())
            .setAccessType(OFFLINE_ACCESS)
            .build())
            .orElse(null);

  }

  public Credential exchangeCode(String code) throws IOException {
    if (gmailFlow.isEmpty()) {
      return null;
    }

    var tokenResponse = gmailFlow.get().newTokenRequest(code)
            .setRedirectUri(callbackUrl())
            .execute();

    return gmailFlow.get().createAndStoreCredential(
            tokenResponse,
            USER_ID
    );
  }

  public boolean isAuthorized() throws IOException {
    if (gmailFlow.isEmpty()) {
      return false;
    }
    return gmailFlow.get().loadCredential(USER_ID) != null;
  }

  @Nullable
  public Credential getCredential() throws IOException {
    if (gmailFlow.isEmpty()) {
      return null;
    }

    return gmailFlow.get().loadCredential(USER_ID);
  }

  private String callbackUrl() {
    return properties.getAuthCallbackUrl()
            + ApiConstants.GMAIL_OAUTH_API_URL
            + "/callback";
  }
}
