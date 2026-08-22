package ee.aleksale.remindly;

import ee.aleksale.remindly.utils.EnvUtils;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
@ConfigurationPropertiesScan
public class RemindlyApplication {

  static void main(String[] args) {
    EnvUtils.loadEnvFile();
    SpringApplication.run(RemindlyApplication.class, args);
  }

}
