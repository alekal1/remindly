package ee.aleksale.remindly.modules.gmail.service.processes.unscheduled_garbage;

import ee.aleksale.remindly.modules.garbage_collection.dto.GarbageCollectionSchedule;
import ee.aleksale.remindly.modules.gmail.service.processes.GmailTableExtractorService;
import ee.aleksale.remindly.utils.DateUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;

@Slf4j
@Service
public class UnscheduledGarbageExtractor implements GmailTableExtractorService<GarbageCollectionSchedule> {

  private static final String TYPE_COL = "type";
  private static final String DATE_COL = "date";

  @Override
  public GarbageCollectionSchedule extract(GmailTableRowContext data) {
    final var values = data.values();
    final var tableConfig = data.context()
            .template()
            .getSelector()
            .getTable();

    return GarbageCollectionSchedule.builder()
            .type(GarbageCollectionSchedule.GarbageType.mapFromString(
                    values.get(TYPE_COL).toLowerCase(Locale.ROOT)))
            .dates(List.of(
                    DateUtils.convertToLocalDate(
                            values.get(DATE_COL).replace(" ", ""),
                            tableConfig.getDateFormat())
            ))
            .build();
  }

}
