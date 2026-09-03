package ee.aleksale.remindly.modules.garbage_collection.gmail.property;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "app.gmail")
public class GmailProperties {
  private String credentialsFile;
  private String tokenDirectory;
  private String sender;
  private String authCallbackUrl;
}
