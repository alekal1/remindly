package ee.aleksale.remindly.modules.calendar.service;

import ee.aleksale.remindly.core.exception.RemindlyException;
import ee.aleksale.remindly.core.model.domain.EventEntity;
import ee.aleksale.remindly.core.model.type.DeliveryChannel;
import ee.aleksale.remindly.core.model.type.EventType;
import ee.aleksale.remindly.core.repository.EventRepository;
import ee.aleksale.remindly.modules.calendar.property.CalendarProperties;
import ee.aleksale.remindly.utils.DateUtils;
import lombok.RequiredArgsConstructor;
import net.fortuna.ical4j.data.CalendarOutputter;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.ParameterList;
import net.fortuna.ical4j.model.component.VAlarm;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.parameter.Value;
import net.fortuna.ical4j.model.property.Description;
import net.fortuna.ical4j.model.property.DtEnd;
import net.fortuna.ical4j.model.property.DtStamp;
import net.fortuna.ical4j.model.property.DtStart;
import net.fortuna.ical4j.model.property.RefreshInterval;
import net.fortuna.ical4j.model.property.Summary;
import net.fortuna.ical4j.model.property.Uid;
import net.fortuna.ical4j.model.property.XProperty;
import net.fortuna.ical4j.model.property.immutable.ImmutableAction;
import net.fortuna.ical4j.model.property.immutable.ImmutableCalScale;
import net.fortuna.ical4j.model.property.immutable.ImmutableTransp;
import net.fortuna.ical4j.model.property.immutable.ImmutableVersion;
import net.fortuna.ical4j.validate.ValidationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import ee.aleksale.remindly.utils.EmojiUtils;

import java.io.IOException;
import java.io.StringWriter;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class CalendarFeedService {

  static final String PROD_ID = "-//remindly//remindly//EN";
  static final String CALENDAR_NAME = "Remindly - Garbage collection";
  private static final Duration REFRESH_INTERVAL = Duration.ofHours(1);

  private final Clock clock;
  private final EventRepository eventRepository;
  private final CalendarProperties properties;

  @Transactional(readOnly = true)
  public String renderFeed() {
    final var from = DateUtils.startOfDayDaysAgo(clock, properties.getPastDays());
    final var events = eventRepository.findAllByTypeInAndScheduledAtAfterOrderByScheduledAtAsc(
            EventType.byChannel(DeliveryChannel.CALENDAR), from);
    final var stamp = Instant.now(clock).truncatedTo(ChronoUnit.SECONDS);

    final var calendar = new Calendar()
            .withProdId(PROD_ID)
            .withProperty(ImmutableVersion.VERSION_2_0)
            .withProperty(ImmutableCalScale.GREGORIAN)
            .withProperty(new XProperty("X-WR-CALNAME", CALENDAR_NAME))
            .withProperty(new XProperty("X-WR-TIMEZONE", DateUtils.TIMEZONE))
            .withProperty(new RefreshInterval(new ParameterList(List.of(Value.DURATION)), REFRESH_INTERVAL))
            .withProperty(new XProperty("X-PUBLISHED-TTL", REFRESH_INTERVAL.toString()))
            .getFluentTarget();

    events.forEach(event -> calendar.add(toVEvent(event, stamp)));

    return write(calendar);
  }

  private VEvent toVEvent(EventEntity event, Instant stamp) {
    final LocalDate day = event.getScheduledAt().toLocalDate();
    final var summary = summary(event.getType());

    final var alarm = new VAlarm(properties.getAlarmTrigger())
            .withProperty(ImmutableAction.DISPLAY)
            .withProperty(new Description(summary))
            .getFluentTarget();

    final var vEvent = (VEvent) new VEvent(false)
            .withProperty(new Uid("remindly-event-" + event.getId() + "@remindly"))
            .withProperty(new DtStamp(stamp))
            .withProperty(new DtStart<>(day))
            .withProperty(new DtEnd<>(day.plusDays(1)))
            .withProperty(new Summary(summary))
            .withProperty(new Description(event.getMessage()))
            .withProperty(ImmutableTransp.TRANSPARENT)
            .getFluentTarget();
    vEvent.add(alarm);

    return vEvent;
  }

  private static String write(Calendar calendar) {
    try (var writer = new StringWriter()) {
      new CalendarOutputter(false).output(calendar, writer);
      return writer.toString();
    } catch (IOException | ValidationException e) {
      throw new RemindlyException("Failed to build calendar feed: " + e.getMessage());
    }
  }

  static String summary(EventType type) {
    final var label = StringUtils.capitalize(type.name().replace('_', ' ').toLowerCase(Locale.ROOT));
    return EmojiUtils.toUnicode(EmojiUtils.getEmojisForEventType(type)) + " " + label;
  }
}
