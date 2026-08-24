package ee.aleksale.remindly.modules.adhoc.service;

import ee.aleksale.remindly.core.client.NtfyClient;
import ee.aleksale.remindly.core.repository.EventRepository;
import ee.aleksale.remindly.modules.adhoc.dto.Adhoc;
import ee.aleksale.remindly.modules.adhoc.dto.mapper.AdhocMapper;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdhocService {

  private final NtfyClient ntfyClient;
  private final EventRepository eventRepository;

  @Transactional
  public void adhoc(Adhoc adhoc) {
    final var entity = AdhocMapper.INSTANCE.map(adhoc);

    eventRepository.saveAndFlush(entity);

    ntfyClient.sendNotification(entity.getType(),
            String.format("Reminder for '%s' is scheduled at %s", entity.getMessage(), entity.getScheduledAt()));
  }
}
