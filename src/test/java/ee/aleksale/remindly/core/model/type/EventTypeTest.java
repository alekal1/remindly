package ee.aleksale.remindly.core.model.type;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class EventTypeTest {

  @Test
  void shouldHaveNtfyTopic_forEveryNtfyEventType() {
    assertThat(EventType.byChannel(DeliveryChannel.NTFY))
            .isNotEmpty()
            .allSatisfy(type -> assertThat(type.getReminderType()).as(type.name()).isNotNull());
  }

  @Test
  void shouldHaveNoNtfyTopic_forCalendarEventTypes() {
    assertThat(EventType.byChannel(DeliveryChannel.CALENDAR))
            .containsExactlyInAnyOrder(
                    EventType.BIO_WASTE_COLLECTION,
                    EventType.MIXED_WASTE_COLLECTION,
                    EventType.PACKAGING_WASTE_COLLECTION)
            .allSatisfy(type -> assertThat(type.getReminderType()).as(type.name()).isNull());
  }
}
