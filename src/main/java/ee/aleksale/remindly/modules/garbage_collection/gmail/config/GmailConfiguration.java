package ee.aleksale.remindly.modules.garbage_collection.gmail.config;

import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.gmail.GmailScopes;
import ee.aleksale.remindly.modules.garbage_collection.gmail.property.GmailProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.util.List;

@Slf4j
@Configuration
@Conditional(GmailCredentialsCondition.class)
@RequiredArgsConstructor
public class GmailConfiguration {
  public static final String USER_ID = "me";

  private final GmailProperties properties;

  @Bean
  public HttpTransport gmailHttpTransport() throws GeneralSecurityException, IOException {
    return GoogleNetHttpTransport.newTrustedTransport();
  }

  @Bean
  public JsonFactory gmailJsonFactory() {
    return GsonFactory.getDefaultInstance();
  }

  @Bean
  public GoogleClientSecrets gmailSecrets(JsonFactory gmailJsonFactory) {
    var credentialsResource = new FileSystemResource(properties.getCredentialsFile());

    try {
      return GoogleClientSecrets.load(
              gmailJsonFactory,
              new InputStreamReader(credentialsResource.getInputStream()));
    } catch (IOException e) {
     log.info("Could not configure gmail secrets, failed to read credentials file: {}", e.getMessage());
     return new GoogleClientSecrets();
    }
  }

  @Bean
  public GoogleAuthorizationCodeFlow gmailFlow(
          GoogleClientSecrets gmailSecrets,
          HttpTransport gmailHttpTransport,
          JsonFactory gmailJsonFactory) {

    var tokenDirectoryFile = new File(properties.getTokenDirectory());
    if (!tokenDirectoryFile.exists() && !tokenDirectoryFile.mkdirs()) {
      log.info("Could not configure gmail flow, failed to create token directory");
      return null;
    }

    if (gmailSecrets.isEmpty()) {
      log.info("Could not configure gmail flow, secrets not configured");
      return null;
    }

    try {
      return new GoogleAuthorizationCodeFlow.Builder(
              gmailHttpTransport,
              gmailJsonFactory,
              gmailSecrets,
              List.of(GmailScopes.GMAIL_READONLY))
              .setDataStoreFactory(new FileDataStoreFactory(tokenDirectoryFile))
              .setAccessType("offline")
              .build();
    } catch (IOException e) {
      log.info("Could not configure gmail flow, failed to create data store factory: {}", e.getMessage());
      return null;
    }
  }
}
