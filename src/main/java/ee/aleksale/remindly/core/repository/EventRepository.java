package ee.aleksale.remindly.core.repository;

import ee.aleksale.remindly.core.model.domain.EventEntity;
import jakarta.persistence.LockModeType;
import ee.aleksale.remindly.core.model.type.EventType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@Repository
public interface EventRepository extends JpaRepository<EventEntity, Long> {

  void deleteAllByTypeIn(Collection<EventType> types);

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  List<EventEntity> findAllBySentAtIsNullAndScheduledAtLessThanEqual(LocalDateTime scheduledAt);

}
