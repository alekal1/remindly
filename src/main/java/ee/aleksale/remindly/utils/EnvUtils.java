package ee.aleksale.remindly.utils;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Slf4j
@UtilityClass
public class EnvUtils {
  private static final String COMMENT = "#";
  private static final String EQUALS = "=";
  private static final String ENV_FILE_PATH_PROPERTY = "remindly.env.file.path";
  private static final String DEFAULT_ENV_FILE_PATH = "secrets/.env";

  public static void loadEnvFile() {
    final var envPath = Path.of(System.getProperty(ENV_FILE_PATH_PROPERTY, DEFAULT_ENV_FILE_PATH));

    if (!Files.exists(envPath)) {
      log.error(".env file not found at {}", envPath.toAbsolutePath());
      return;
    }

    try {
      Files.readAllLines(envPath).forEach(line -> {
        line = line.trim();
        if (line.isEmpty() || line.startsWith(COMMENT)) {
          return;
        }

        var eq = line.indexOf(EQUALS);
        if (eq > 0) {
          var key = line.substring(0, eq).trim();
          var value = line.substring(eq + 1).trim();

          if (System.getProperty(key) == null && System.getenv(key) == null) {
            System.setProperty(key, value);
          }
        }
      });
      log.info("Loaded .env file from {}", envPath.toAbsolutePath());
    } catch (IOException e) {
      log.error("Failed to read .env file: {}", e.getMessage());
    }
  }
}
