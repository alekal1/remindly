package ee.aleksale.remindly.modules.garbage_collection.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import ee.aleksale.remindly.core.client.NtfyClient;
import ee.aleksale.remindly.core.exception.RemindlyException;
import ee.aleksale.remindly.core.model.domain.EventEntity;
import ee.aleksale.remindly.core.model.type.EventType;
import ee.aleksale.remindly.core.repository.EventRepository;
import ee.aleksale.remindly.modules.garbage_collection.dto.GarbageCollectionSchedule;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

@ExtendWith(MockitoExtension.class)
public class GarbageCollectionServiceTest {

  private NtfyClient ntfyClient;
  private NtfyClient.NtfyRequestBuilder ntfyRequestBuilder;
  private GarbageScheduleExtractorService garbageScheduleExtractorService;
  private EventRepository eventRepository;

  private GarbageCollectionService garbageCollectionService;

  private ArgumentCaptor<List<EventEntity>> eventListCaptor;

  @BeforeEach
  void init() {
    ntfyClient = mock(NtfyClient.class);
    ntfyRequestBuilder = mock(NtfyClient.NtfyRequestBuilder.class);
    garbageScheduleExtractorService = mock(GarbageScheduleExtractorService.class);
    eventRepository = mock(EventRepository.class);

    garbageCollectionService =
        new GarbageCollectionService(ntfyClient, garbageScheduleExtractorService, eventRepository);

    eventListCaptor = ArgumentCaptor.forClass(List.class);
  }

  @Test
  void shouldResetAndExtractSchedules_whenFileIsProvided() throws IOException {
    final var file = mock(MultipartFile.class);
    final var fileBytes = "test bytes".getBytes();
    final var bioDate = LocalDate.of(2024, 12, 10);
    final var mixedDate = LocalDate.of(2024, 12, 17);
    final var packagingDate = LocalDate.of(2024, 12, 24);

    final var schedules = Arrays.asList(
        GarbageCollectionSchedule.builder()
            .type(GarbageCollectionSchedule.GarbageType.BIO)
            .dates(List.of(bioDate))
            .build(),
        GarbageCollectionSchedule.builder()
            .type(GarbageCollectionSchedule.GarbageType.MIXED)
            .dates(List.of(mixedDate))
            .build(),
        GarbageCollectionSchedule.builder()
            .type(GarbageCollectionSchedule.GarbageType.PACKAGING)
            .dates(List.of(packagingDate))
            .build()
    );

    doReturn(fileBytes).when(file).getBytes();
    doReturn(schedules).when(garbageScheduleExtractorService).extract(fileBytes);
    doReturn(ntfyRequestBuilder).when(ntfyClient).notification(any(EventType.class));
    doReturn(ntfyRequestBuilder).when(ntfyRequestBuilder).withMessage(anyString());
    doReturn(ntfyRequestBuilder).when(ntfyRequestBuilder).withDefaultEmojis();

    garbageCollectionService.resetAndExtractSchedules(file);

    verify(eventRepository).deleteAllByTypeIn(anyList());
    verify(eventRepository).saveAll(eventListCaptor.capture());
    verify(ntfyClient).notification(EventType.GARBAGE_SCHEDULE_RESET);
    verify(ntfyRequestBuilder).withMessage(anyString());
    verify(ntfyRequestBuilder).withDefaultEmojis();

    final var savedEntities = eventListCaptor.getValue();
    assertEquals(3, savedEntities.size());

    final var bioEntity = savedEntities.stream()
        .filter(e -> e.getType() == EventType.BIO_WASTE_COLLECTION)
        .findFirst()
        .orElseThrow();
    assertEquals("Bio waste collection is scheduled for tomorrow.", bioEntity.getMessage());
    assertNotNull(bioEntity.getScheduledAt());
  }

  @Test
  void shouldHandleMultipleDatesPerGarbageType_whenExtractingSchedules() throws IOException {
    final var file = mock(MultipartFile.class);
    final var fileBytes = "test bytes".getBytes();
    final var bioDate1 = LocalDate.of(2024, 12, 10);
    final var bioDate2 = LocalDate.of(2024, 12, 24);

    final var schedules = List.of(
        GarbageCollectionSchedule.builder()
            .type(GarbageCollectionSchedule.GarbageType.BIO)
            .dates(Arrays.asList(bioDate1, bioDate2))
            .build()
    );

    doReturn(fileBytes).when(file).getBytes();
    doReturn(schedules).when(garbageScheduleExtractorService).extract(fileBytes);
    doReturn(ntfyRequestBuilder).when(ntfyClient).notification(any(EventType.class));
    doReturn(ntfyRequestBuilder).when(ntfyRequestBuilder).withMessage(anyString());
    doReturn(ntfyRequestBuilder).when(ntfyRequestBuilder).withDefaultEmojis();

    garbageCollectionService.resetAndExtractSchedules(file);

    verify(eventRepository).saveAll(eventListCaptor.capture());

    final var savedEntities = eventListCaptor.getValue();
    assertEquals(2, savedEntities.size());
    assertEquals(2, savedEntities.stream()
        .filter(e -> e.getType() == EventType.BIO_WASTE_COLLECTION)
        .count());
  }

  @Test
  void shouldThrowRemindlyException_whenFileReadFails() throws IOException {
    final var file = mock(MultipartFile.class);
    doThrow(new IOException("File read error")).when(file).getBytes();

    try {
      garbageCollectionService.resetAndExtractSchedules(file);
    } catch (RemindlyException e) {
      assertEquals("File read error", e.getMessage());
    }
  }

  @Test
  void shouldMapCorrectMessages_forEachGarbageType() throws IOException {
    final var file = mock(MultipartFile.class);
    final var fileBytes = "test bytes".getBytes();
    final var testDate = LocalDate.of(2024, 12, 10);

    final var schedules = Arrays.asList(
        GarbageCollectionSchedule.builder()
            .type(GarbageCollectionSchedule.GarbageType.BIO)
            .dates(List.of(testDate))
            .build(),
        GarbageCollectionSchedule.builder()
            .type(GarbageCollectionSchedule.GarbageType.MIXED)
            .dates(List.of(testDate))
            .build(),
        GarbageCollectionSchedule.builder()
            .type(GarbageCollectionSchedule.GarbageType.PACKAGING)
            .dates(List.of(testDate))
            .build()
    );

    doReturn(fileBytes).when(file).getBytes();
    doReturn(schedules).when(garbageScheduleExtractorService).extract(fileBytes);
    doReturn(ntfyRequestBuilder).when(ntfyClient).notification(any(EventType.class));
    doReturn(ntfyRequestBuilder).when(ntfyRequestBuilder).withMessage(anyString());
    doReturn(ntfyRequestBuilder).when(ntfyRequestBuilder).withDefaultEmojis();

    garbageCollectionService.resetAndExtractSchedules(file);

    verify(eventRepository).saveAll(eventListCaptor.capture());

    final var savedEntities = eventListCaptor.getValue();
    assertEquals("Bio waste collection is scheduled for tomorrow.",
        savedEntities.stream()
            .filter(e -> e.getType() == EventType.BIO_WASTE_COLLECTION)
            .findFirst()
            .orElseThrow()
            .getMessage());
    assertEquals("Mixed waste collection is scheduled for tomorrow.",
        savedEntities.stream()
            .filter(e -> e.getType() == EventType.MIXED_WASTE_COLLECTION)
            .findFirst()
            .orElseThrow()
            .getMessage());
    assertEquals("Packaging waste collection is scheduled for tomorrow.",
        savedEntities.stream()
            .filter(e -> e.getType() == EventType.PACKAGING_WASTE_COLLECTION)
            .findFirst()
            .orElseThrow()
            .getMessage());
  }
}
