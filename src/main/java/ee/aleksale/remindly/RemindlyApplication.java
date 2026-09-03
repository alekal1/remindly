package ee.aleksale.remindly;

import ee.aleksale.remindly.core.config.EnvApplicationContextInitializer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
@ConfigurationPropertiesScan
public class RemindlyApplication {

  static void main(String[] args) {
    SpringApplication application = new SpringApplication(RemindlyApplication.class);
    application.addInitializers(new EnvApplicationContextInitializer());
    application.run(args);
  }

}
