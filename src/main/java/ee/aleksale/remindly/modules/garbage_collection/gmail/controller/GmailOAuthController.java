package ee.aleksale.remindly.modules.garbage_collection.gmail.controller;

import ee.aleksale.remindly.core.constants.ApiConstants;
import ee.aleksale.remindly.modules.garbage_collection.gmail.service.GmailOAuthService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@RequestMapping(value = ApiConstants.GMAIL_OAUTH_API_URL)
@RequiredArgsConstructor
public class GmailOAuthController {

  private final GmailOAuthService oauthService;

  @GetMapping
  public void auth(HttpServletResponse response) throws IOException {
    if (oauthService.isAuthorized()) {
      response.sendRedirect(ApiConstants.GMAIL_OAUTH_API_URL + "/callback");
      return;
    }

    response.sendRedirect(oauthService.authorizationUrl());
  }

  @GetMapping("/callback")
  public String callback(
          @RequestParam String code
  ) throws IOException {

    var credential = oauthService.exchangeCode(code);

    if (credential == null) {
      return """
        <html>
        <body>
            <h1>Gmail authorization failed</h1>
        </body>
        </html>
        """;
    }

    return """
        <html>
        <body>
            <h1>Gmail authorization successful</h1>
            <p>Access token: %s</p>
            <p>Refresh token: %s</p>
        </body>
        </html>
        """.formatted(credential.getAccessToken() != null, credential.getRefreshToken() != null
    );
  }
}
