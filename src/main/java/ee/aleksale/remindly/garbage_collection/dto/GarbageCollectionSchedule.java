package ee.aleksale.remindly.garbage_collection.dto;

import ee.aleksale.remindly.core.model.type.EventType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GarbageCollectionSchedule {

  private GarbageType type;
  private List<LocalDate> dates;

  public enum GarbageType {
    BIO,
    MIXED,
    PACKAGING;


    public EventType mapToEventType() {
      return switch (this) {
        case BIO -> EventType.BIO_WASTE_COLLECTION;
        case MIXED -> EventType.MIXED_WASTE_COLLECTION;
        case PACKAGING -> EventType.PACKAGING_WASTE_COLLECTION;
      };
    }
  }

}
