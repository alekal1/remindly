package ee.aleksale.remindly.core.config;

import ee.aleksale.remindly.utils.EnvUtils;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;

public class EnvApplicationContextInitializer
        implements ApplicationContextInitializer<ConfigurableApplicationContext> {

  @Override
  public void initialize(ConfigurableApplicationContext applicationContext) {
    EnvUtils.loadEnvFile();
  }
}
