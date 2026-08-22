package ee.aleksale.remindly.core.model.type;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum NtfyTopic {
  ERRORS("errors"),
  GARBAGE_COLLECTION("garbage-collection");

  private final String configKey;
}
