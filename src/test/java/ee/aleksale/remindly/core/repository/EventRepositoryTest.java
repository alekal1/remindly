package ee.aleksale.remindly.core.repository;

import ee.aleksale.remindly.core.config.EnvApplicationContextInitializer;
import ee.aleksale.remindly.core.model.domain.EventEntity;
import ee.aleksale.remindly.core.model.type.EventType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@SpringBootTest
@ContextConfiguration(initializers = EnvApplicationContextInitializer.class)
@Transactional
class EventRepositoryTest {

  @Container
  static final PostgreSQLContainer<?> POSTGRESQL_CONTAINER =
          new PostgreSQLContainer<>("postgres:16-alpine")
                  .withDatabaseName("remindlydb")
                  .withUsername("remindly")
                  .withPassword("remindly");

  @Autowired
  private EventRepository eventRepository;

  @BeforeEach
  void cleanDatabase() {
    eventRepository.deleteAll();
  }

  @DynamicPropertySource
  static void configureProperties(DynamicPropertyRegistry registry) {
    registry.add("spring.datasource.url", POSTGRESQL_CONTAINER::getJdbcUrl);
    registry.add("spring.datasource.username", POSTGRESQL_CONTAINER::getUsername);
    registry.add("spring.datasource.password", POSTGRESQL_CONTAINER::getPassword);
    registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
    registry.add("spring.liquibase.enabled", () -> "false");
    registry.add("spring.task.scheduling.enabled", () -> "false");
  }

  @Test
  void shouldFindOnlyDueAndUnsentEvents() {
    final var now = LocalDateTime.of(2026, 8, 25, 12, 30);
    final var dueUnsentBefore = eventRepository.save(event(EventType.ADHOC, now.minusMinutes(5), null, "due-before"));
    final var dueUnsentAt = eventRepository.save(event(EventType.REMINDLY_APP_ERROR, now, null, "due-at"));
    eventRepository.save(event(EventType.GARBAGE_SCHEDULE_RESET, now.plusMinutes(1), null, "future"));
    eventRepository.save(event(EventType.BIO_WASTE_COLLECTION, now.minusMinutes(1), now.minusMinutes(1), "already-sent"));

    final var dueEvents = eventRepository.findAllBySentAtIsNullAndScheduledAtLessThanEqual(now);

    assertThat(dueEvents)
            .extracting(EventEntity::getId)
            .containsExactlyInAnyOrder(dueUnsentBefore.getId(), dueUnsentAt.getId());
  }

  @Test
  void shouldDeleteOnlyEventsMatchingTypes() {
    eventRepository.save(event(EventType.ADHOC, LocalDateTime.of(2026, 8, 25, 10, 0), null, "adhoc"));
    eventRepository.save(event(EventType.BIO_WASTE_COLLECTION, LocalDateTime.of(2026, 8, 25, 11, 0), null, "bio"));
    eventRepository.save(event(EventType.MIXED_WASTE_COLLECTION, LocalDateTime.of(2026, 8, 25, 12, 0), null, "mixed"));

    eventRepository.deleteAllByTypeIn(List.of(EventType.ADHOC, EventType.BIO_WASTE_COLLECTION));

    final var remainingEvents = eventRepository.findAll();

    assertThat(remainingEvents)
            .hasSize(1)
            .extracting(EventEntity::getType)
            .containsExactly(EventType.MIXED_WASTE_COLLECTION);
  }

  @Test
  void shouldFindOnlyMatchingTypesScheduledAfterGivenTime_orderedAscending_andLimitedByPageable() {
    final var now = LocalDateTime.of(2026, 8, 25, 12, 30);
    eventRepository.save(event(EventType.ADHOC, now.plusMinutes(5), null, "wrong-type"));
    eventRepository.save(event(EventType.BIO_WASTE_COLLECTION, now.minusMinutes(5), null, "before-now"));
    final var mixedSoon = eventRepository.save(event(EventType.MIXED_WASTE_COLLECTION, now.plusMinutes(10), null, "mixed-soon"));
    final var bioLater = eventRepository.save(event(EventType.BIO_WASTE_COLLECTION, now.plusMinutes(20), null, "bio-later"));
    eventRepository.save(event(EventType.PACKAGING_WASTE_COLLECTION, now.plusMinutes(30), null, "packaging-latest"));

    final var events = eventRepository.findAllByTypeInAndScheduledAtAfter(
            List.of(EventType.BIO_WASTE_COLLECTION, EventType.MIXED_WASTE_COLLECTION),
            now,
            PageRequest.of(0, 2, Sort.by(Sort.Direction.ASC, "scheduledAt")));

    assertThat(events)
            .extracting(EventEntity::getId)
            .containsExactly(mixedSoon.getId(), bioLater.getId());
  }

  private static EventEntity event(EventType type, LocalDateTime scheduledAt, LocalDateTime sentAt, String message) {
    return EventEntity.builder()
            .type(type)
            .scheduledAt(scheduledAt)
            .sentAt(sentAt)
            .message(message)
            .build();
  }
}
