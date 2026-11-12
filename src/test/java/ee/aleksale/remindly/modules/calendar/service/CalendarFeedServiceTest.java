package ee.aleksale.remindly.modules.calendar.service;

import ee.aleksale.remindly.core.model.domain.EventEntity;
import ee.aleksale.remindly.core.model.type.DeliveryChannel;
import ee.aleksale.remindly.core.model.type.EventType;
import ee.aleksale.remindly.core.repository.EventRepository;
import ee.aleksale.remindly.modules.calendar.property.CalendarProperties;
import net.fortuna.ical4j.data.CalendarBuilder;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.Property;
import net.fortuna.ical4j.model.component.VAlarm;
import net.fortuna.ical4j.model.component.VEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class CalendarFeedServiceTest {

  private final Clock clock = Clock.fixed(Instant.parse("2026-08-25T10:30:15Z"), ZoneOffset.UTC);

  private EventRepository eventRepository;
  private CalendarProperties properties;
  private CalendarFeedService service;

  @BeforeEach
  void init() {
    eventRepository = mock(EventRepository.class);
    properties = new CalendarProperties();
    service = new CalendarFeedService(clock, eventRepository, properties);
  }

  @Test
  void shouldRenderCalendarHeader() throws Exception {
    givenEvents();

    final var ics = service.renderFeed();
    final var calendar = parse(ics);

    assertThat(value(calendar, Property.PRODID)).isEqualTo(CalendarFeedService.PROD_ID);
    assertThat(value(calendar, Property.VERSION)).isEqualTo("2.0");
    assertThat(value(calendar, Property.CALSCALE)).isEqualTo("GREGORIAN");
    assertThat(value(calendar, Property.METHOD)).isNull();
    assertThat(value(calendar, "X-WR-CALNAME")).isEqualTo(CalendarFeedService.CALENDAR_NAME);
    assertThat(ics).contains("REFRESH-INTERVAL;VALUE=DURATION:PT1H\r\n");
    assertThat(calendar.getComponents()).isEmpty();
  }

  @Test
  void shouldRenderAllDayEventWithAlarm() throws Exception {
    givenEvents(event(42L, EventType.BIO_WASTE_COLLECTION, LocalDateTime.of(2026, 8, 27, 8, 0),
            "Bio waste collection is scheduled for today."));

    final var ics = service.renderFeed();

    assertThat(ics)
            .contains("DTSTAMP:20260825T103015Z\r\n")
            .contains("DTSTART;VALUE=DATE:20260827\r\n")
            .contains("DTEND;VALUE=DATE:20260828\r\n")
            .contains("TRIGGER:PT8H\r\n");

    final List<VEvent> events = parse(ics).getComponents(VEvent.VEVENT);
    assertThat(events).hasSize(1);
    final var vEvent = events.getFirst();
    assertThat(value(vEvent, Property.UID)).isEqualTo("remindly-event-42@remindly");
    assertThat(value(vEvent, Property.SUMMARY)).isEqualTo("🌱🍃 Bio waste collection");
    assertThat(value(vEvent, Property.DESCRIPTION)).isEqualTo("Bio waste collection is scheduled for today.");
    assertThat(value(vEvent, Property.TRANSP)).isEqualTo("TRANSPARENT");

    final List<VAlarm> alarms = vEvent.getComponents(VAlarm.VALARM);
    assertThat(alarms).hasSize(1);
    assertThat(value(alarms.getFirst(), Property.ACTION)).isEqualTo("DISPLAY");
  }

  @Test
  void shouldRenderNegativeAlarmTrigger() {
    properties.setAlarmTrigger(Duration.ofHours(-4));
    givenEvents(event(1L, EventType.MIXED_WASTE_COLLECTION, LocalDateTime.of(2026, 8, 27, 8, 0), "msg"));

    assertThat(service.renderFeed()).contains("TRIGGER:-PT4H\r\n");
  }

  @Test
  void shouldEscapeTextAndFoldLongLines() throws Exception {
    final var message = "Bins, bags; and \\ a very long description that certainly exceeds seventy five octets 📦";
    givenEvents(event(7L, EventType.PACKAGING_WASTE_COLLECTION, LocalDateTime.of(2026, 8, 27, 8, 0), message));

    final var ics = service.renderFeed();

    assertThat(ics).contains("Bins\\, bags\\; and \\\\ a very long");
    assertThat(Arrays.stream(ics.split("\r\n")))
            .allMatch(line -> line.getBytes(StandardCharsets.UTF_8).length <= 75);
    assertThat(ics.replace("\r\n", "")).doesNotContain("\n");
    assertThat(value(parse(ics).getComponents(VEvent.VEVENT).getFirst(), Property.DESCRIPTION)).isEqualTo(message);
  }

  @Test
  void shouldQueryOnlyCalendarTypesFromPastDays() {
    properties.setPastDays(10);
    givenEvents();

    service.renderFeed();

    verify(eventRepository).findAllByTypeInAndScheduledAtAfterOrderByScheduledAtAsc(
            EventType.byChannel(DeliveryChannel.CALENDAR),
            LocalDateTime.of(2026, 8, 15, 0, 0));
    assertThat(EventType.byChannel(DeliveryChannel.CALENDAR)).containsExactlyInAnyOrder(
            EventType.BIO_WASTE_COLLECTION,
            EventType.MIXED_WASTE_COLLECTION,
            EventType.PACKAGING_WASTE_COLLECTION);
  }

  private void givenEvents(EventEntity... events) {
    doReturn(List.of(events)).when(eventRepository)
            .findAllByTypeInAndScheduledAtAfterOrderByScheduledAtAsc(anyCollection(), any());
  }

  private static EventEntity event(Long id, EventType type, LocalDateTime scheduledAt, String message) {
    return EventEntity.builder().id(id).type(type).scheduledAt(scheduledAt).message(message).build();
  }

  private static Calendar parse(String ics) throws Exception {
    return new CalendarBuilder().build(new StringReader(ics));
  }

  private static String value(net.fortuna.ical4j.model.PropertyContainer container, String name) {
    return container.getProperty(name).map(Property::getValue).orElse(null);
  }
}
