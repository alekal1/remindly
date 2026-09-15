package ee.aleksale.remindly.core.model.type;

import ee.aleksale.remindly.core.exception.RemindlyException;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Getter
@RequiredArgsConstructor
public enum ReminderType {
  ERRORS("errors"),
  GARBAGE_COLLECTION("garbage-collection"),
  ADHOC("adhoc");

  private final String id;

  private static final Map<String, ReminderType> BY_ID = Arrays.stream(values())
          .collect(Collectors.toUnmodifiableMap(ReminderType::getId, Function.identity()));

  public static ReminderType fromId(String id) {
    final var type = BY_ID.get(id);

    if (type == null) {
      throw new RemindlyException("ReminderType not found for id: " + id);
    }

    return type;
  }

}
