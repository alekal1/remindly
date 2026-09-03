package ee.aleksale.remindly.modules.garbage_collection.gmail.service;

import static ee.aleksale.remindly.modules.garbage_collection.gmail.config.GmailConfiguration.USER_ID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeRequestUrl;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeTokenRequest;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import ee.aleksale.remindly.core.constants.ApiConstants;
import ee.aleksale.remindly.modules.garbage_collection.gmail.property.GmailProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
class GmailOAuthServiceTest {

  private static final String CALLBACK_URL = "http://localhost:8080" + ApiConstants.GMAIL_OAUTH_API_URL + "/callback";

  private GoogleAuthorizationCodeFlow gmailFlow;
  private GmailProperties properties;

  @BeforeEach
  void init() {
    gmailFlow = mock(GoogleAuthorizationCodeFlow.class);
    properties = mock(GmailProperties.class);
  }

  @Test
  void shouldBuildAuthorizationUrl_whenFlowIsAvailable() {
    final var authorizationUrl = mock(GoogleAuthorizationCodeRequestUrl.class);
    final var gmailOAuthService = new GmailOAuthService(Optional.of(gmailFlow), properties);

    doReturn("http://localhost:8080").when(properties).getAuthCallbackUrl();
    doReturn(authorizationUrl).when(gmailFlow).newAuthorizationUrl();
    doReturn(authorizationUrl).when(authorizationUrl).setRedirectUri(CALLBACK_URL);
    doReturn(authorizationUrl).when(authorizationUrl).setAccessType("offline");
    doReturn("https://accounts.google.com/o/oauth2/auth").when(authorizationUrl).build();

    final var result = gmailOAuthService.authorizationUrl();

    assertEquals("https://accounts.google.com/o/oauth2/auth", result);
    verify(gmailFlow).newAuthorizationUrl();
    verify(authorizationUrl).setRedirectUri(CALLBACK_URL);
    verify(authorizationUrl).setAccessType("offline");
  }

  @Test
  void shouldReturnNullAuthorizationUrl_whenFlowIsMissing() {
    final var gmailOAuthService = new GmailOAuthService(Optional.empty(), properties);

    assertNull(gmailOAuthService.authorizationUrl());
  }

  @Test
  void shouldExchangeCodeAndStoreCredential_whenFlowIsAvailable() throws IOException {
    final var tokenRequest = mock(GoogleAuthorizationCodeTokenRequest.class);
    final var tokenResponse = mock(GoogleTokenResponse.class);
    final var credential = mock(Credential.class);
    final var gmailOAuthService = new GmailOAuthService(Optional.of(gmailFlow), properties);

    doReturn("http://localhost:8080").when(properties).getAuthCallbackUrl();
    doReturn(tokenRequest).when(gmailFlow).newTokenRequest("auth-code");
    doReturn(tokenRequest).when(tokenRequest).setRedirectUri(CALLBACK_URL);
    doReturn(tokenResponse).when(tokenRequest).execute();
    doReturn(credential).when(gmailFlow).createAndStoreCredential(tokenResponse, USER_ID);

    final var result = gmailOAuthService.exchangeCode("auth-code");

    assertSame(credential, result);
    verify(gmailFlow).newTokenRequest("auth-code");
    verify(tokenRequest).setRedirectUri(CALLBACK_URL);
    verify(tokenRequest).execute();
    verify(gmailFlow).createAndStoreCredential(tokenResponse, USER_ID);
  }

  @Test
  void shouldReturnFalse_whenFlowIsMissing() throws IOException {
    final var gmailOAuthService = new GmailOAuthService(Optional.empty(), properties);

    assertFalse(gmailOAuthService.isAuthorized());
  }

  @Test
  void shouldReturnTrue_whenCredentialExists() throws IOException {
    final var credential = mock(Credential.class);
    final var gmailOAuthService = new GmailOAuthService(Optional.of(gmailFlow), properties);

    doReturn(credential).when(gmailFlow).loadCredential(USER_ID);

    assertTrue(gmailOAuthService.isAuthorized());
    assertSame(credential, gmailOAuthService.getCredential());
  }

  @Test
  void shouldReturnNullCredential_whenFlowIsMissing() throws IOException {
    final var gmailOAuthService = new GmailOAuthService(Optional.empty(), properties);

    assertNull(gmailOAuthService.getCredential());
  }
}
