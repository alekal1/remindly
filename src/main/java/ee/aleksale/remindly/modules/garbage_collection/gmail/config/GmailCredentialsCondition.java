package ee.aleksale.remindly.modules.garbage_collection.gmail.config;

import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

import java.nio.file.Files;
import java.nio.file.Path;

public class GmailCredentialsCondition implements Condition {
  @Override
  public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
    try {
      final var path = context.getEnvironment().getProperty("app.gmail.credentials-file");

      if (path == null || path.isBlank()) {
        return false;
      }

      return Files.isRegularFile(Path.of(path));
    } catch (IllegalArgumentException e) {
      return false;
    }
  }
}
