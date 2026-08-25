package ee.aleksale.remindly.utils;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class EnvUtilsTest {

  private static final String ENV_FILE_PATH_PROPERTY = "remindly.env.file.path";
  private static final String TEST_ENV_PATH = "src/test/resources/.env.test";
  private static final String KEY1 = "ENV_UTILS_TEST_KEY_1";
  private static final String KEY2 = "ENV_UTILS_TEST_KEY_2";
  private static final String KEY_EXISTING = "ENV_UTILS_TEST_KEY_EXISTING";

  @AfterEach
  void cleanup() {
    System.clearProperty(ENV_FILE_PATH_PROPERTY);
    System.clearProperty(KEY1);
    System.clearProperty(KEY2);
    System.clearProperty(KEY_EXISTING);
  }

  @Test
  void shouldLoadPropertiesFromEnvFile() {
    System.setProperty(ENV_FILE_PATH_PROPERTY, TEST_ENV_PATH);

    EnvUtils.loadEnvFile();

    assertEquals("value1", System.getProperty(KEY1));
    assertEquals("value2", System.getProperty(KEY2));
  }

  @Test
  void shouldNotOverrideAlreadySetSystemProperty() {
    System.setProperty(ENV_FILE_PATH_PROPERTY, TEST_ENV_PATH);
    System.setProperty(KEY_EXISTING, "existing-value");

    EnvUtils.loadEnvFile();

    assertEquals("existing-value", System.getProperty(KEY_EXISTING));
  }

  @Test
  void shouldIgnoreMalformedLinesWithoutEqualsSign() {
    System.setProperty(ENV_FILE_PATH_PROPERTY, TEST_ENV_PATH);
    System.clearProperty("ENV_UTILS_TEST_MALFORMED");

    EnvUtils.loadEnvFile();

    assertNull(System.getProperty("ENV_UTILS_TEST_MALFORMED"));
  }
}
