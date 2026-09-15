package ee.aleksale.remindly.modules.gmail.model;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Component;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class GmailTemplateLoader {

  private static final String TEMPLATES_LOCATION_PATTERN = "classpath*:gmail-templates/*.yml";

  private final Environment environment;
  private final Yaml yaml;

  @Getter
  private List<GmailTemplate> templates = List.of();

  @PostConstruct
  void loadTemplates() {
    final var resolver = new PathMatchingResourcePatternResolver();
    final List<GmailTemplate> loaded = new ArrayList<>();

    try {
      for (Resource resource : resolver.getResources(TEMPLATES_LOCATION_PATTERN)) {
        try {
          final var raw = new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
          final var resolved = environment.resolveRequiredPlaceholders(raw);
          final var template = yaml.loadAs(resolved, GmailTemplate.class);

          if (!isValid(template)) {
            log.warn("Skipping invalid gmail template: {}", resource.getFilename());
            continue;
          }

          loaded.add(template);
        } catch (Exception e) {
          log.error("Failed to load gmail template {}: {}", resource.getFilename(), e.getMessage());
        }
      }
    } catch (IOException e) {
      log.error("Failed to scan gmail templates: {}", e.getMessage());
    }

    templates = List.copyOf(loaded);
    log.info("Loaded {} gmail template(s)", templates.size());
  }

  private static boolean isValid(GmailTemplate template) {
    if (template == null || template.getSender() == null || template.getSelector() == null) {
      return false;
    }

    final var selector = template.getSelector();

    return selector.getTable() != null || selector.getLink() != null;
  }
}
