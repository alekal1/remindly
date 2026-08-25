package ee.aleksale.remindly.core.aspect;

import ee.aleksale.remindly.core.annotation.ReminderEnabled;
import ee.aleksale.remindly.core.exception.RemindlyException;
import ee.aleksale.remindly.core.model.type.ReminderType;
import ee.aleksale.remindly.core.property.RemindlyAppProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class ReminderEnabledAspectTest {

  private RemindlyAppProperties properties;
  private ReminderEnabledAspect reminderEnabledAspect;

  @BeforeEach
  void init() {
    properties = mock(RemindlyAppProperties.class);
    reminderEnabledAspect = new ReminderEnabledAspect(properties);
  }

  @Test
  void shouldProceed_whenReminderIsEnabled() {
    final var reminder = reminder(true);
    doReturn(reminder).when(properties).getReminders(ReminderType.ADHOC);

    assertDoesNotThrow(() -> reminderEnabledAspect.checkReminderEnabled(annotation(ReminderType.ADHOC)));
  }

  @Test
  void shouldThrow_whenReminderIsDisabled() {
    final var reminder = reminder(false);
    doReturn(reminder).when(properties).getReminders(ReminderType.GARBAGE_COLLECTION);

    final var exception = assertThrows(
            RemindlyException.class,
            () -> reminderEnabledAspect.checkReminderEnabled(annotation(ReminderType.GARBAGE_COLLECTION))
    );

    assertEquals("Reminder 'GARBAGE_COLLECTION' is disabled.", exception.getMessage());
  }

  private static RemindlyAppProperties.ReminderProperties reminder(boolean enabled) {
    final var reminder = new RemindlyAppProperties.ReminderProperties();
    reminder.setEnabled(enabled);
    return reminder;
  }

  private static ReminderEnabled annotation(ReminderType type) {
    return new ReminderEnabled() {
      @Override
      public ReminderType type() {
        return type;
      }

      @Override
      public Class<? extends java.lang.annotation.Annotation> annotationType() {
        return ReminderEnabled.class;
      }
    };
  }
}
