package ee.aleksale.remindly.modules.adhoc.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import ee.aleksale.remindly.core.client.NtfyClient;
import ee.aleksale.remindly.core.model.domain.EventEntity;
import ee.aleksale.remindly.core.model.type.EventType;
import ee.aleksale.remindly.core.repository.EventRepository;
import ee.aleksale.remindly.modules.adhoc.dto.Adhoc;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class AdhocServiceTest {

  private NtfyClient ntfyClient;
  private NtfyClient.NtfyRequestBuilder ntfyRequestBuilder;
  private EventRepository eventRepository;

  private AdhocService adhocService;

  private ArgumentCaptor<EventEntity> eventCaptor;

  @BeforeEach
  void init() {
    ntfyClient = mock(NtfyClient.class);
    ntfyRequestBuilder = mock(NtfyClient.NtfyRequestBuilder.class);
    eventRepository = mock(EventRepository.class);

    adhocService = new AdhocService(ntfyClient, eventRepository);

    eventCaptor = ArgumentCaptor.forClass(EventEntity.class);
  }

  @Test
  void shouldMapAdhocToEventEntity_whenAdhocIsCalled() {
    final var dto = Adhoc.builder().message("test").build();

    doReturn(ntfyRequestBuilder).when(ntfyClient).notification(any(EventType.class));
    doReturn(ntfyRequestBuilder).when(ntfyRequestBuilder).withTitle(anyString());
    doReturn(ntfyRequestBuilder).when(ntfyRequestBuilder).withMessage(anyString());
    doReturn(ntfyRequestBuilder).when(ntfyRequestBuilder).withDefaultEmojis();

    adhocService.adhoc(dto);

    verify(eventRepository).saveAndFlush(eventCaptor.capture());
    verify(ntfyClient).notification(eventCaptor.getValue().getType());

    final var savedEntity = eventCaptor.getValue();
    assertEquals(EventType.ADHOC, savedEntity.getType());
  }

}
