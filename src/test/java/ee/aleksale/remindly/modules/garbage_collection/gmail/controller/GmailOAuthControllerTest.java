package ee.aleksale.remindly.modules.garbage_collection.gmail.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import com.google.api.client.auth.oauth2.Credential;
import ee.aleksale.remindly.core.constants.ApiConstants;
import ee.aleksale.remindly.modules.garbage_collection.gmail.service.GmailOAuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletResponse;

@ExtendWith(MockitoExtension.class)
class GmailOAuthControllerTest {

  private GmailOAuthService gmailOAuthService;
  private GmailOAuthController gmailOAuthController;

  @BeforeEach
  void init() {
    gmailOAuthService = mock(GmailOAuthService.class);
    gmailOAuthController = new GmailOAuthController(gmailOAuthService);
  }

  @Test
  void shouldRedirectToCallback_whenAlreadyAuthorized() throws Exception {
    final var response = new MockHttpServletResponse();
    doReturn(true).when(gmailOAuthService).isAuthorized();

    gmailOAuthController.auth(response);

    assertEquals(ApiConstants.GMAIL_OAUTH_API_URL + "/callback", response.getRedirectedUrl());
  }

  @Test
  void shouldRedirectToGoogleAuthorizationUrl_whenNotAuthorized() throws Exception {
    final var response = new MockHttpServletResponse();
    doReturn(false).when(gmailOAuthService).isAuthorized();
    doReturn("https://accounts.google.com/o/oauth2/auth").when(gmailOAuthService).authorizationUrl();

    gmailOAuthController.auth(response);

    assertEquals("https://accounts.google.com/o/oauth2/auth", response.getRedirectedUrl());
  }

  @Test
  void shouldReturnFailurePage_whenCodeExchangeFails() throws Exception {
    doReturn(null).when(gmailOAuthService).exchangeCode("auth-code");

    final var result = gmailOAuthController.callback("auth-code");

    assertTrue(result.contains("Gmail authorization failed"));
  }

  @Test
  void shouldReturnSuccessPage_whenCodeExchangeSucceeds() throws Exception {
    final var credential = mock(Credential.class);
    doReturn("access-token").when(credential).getAccessToken();
    doReturn("refresh-token").when(credential).getRefreshToken();
    doReturn(credential).when(gmailOAuthService).exchangeCode("auth-code");

    final var result = gmailOAuthController.callback("auth-code");

    assertTrue(result.contains("Gmail authorization successful"));
    assertTrue(result.contains("Access token: true"));
    assertTrue(result.contains("Refresh token: true"));
  }
}
