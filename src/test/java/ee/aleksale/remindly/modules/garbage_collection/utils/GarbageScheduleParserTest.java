package ee.aleksale.remindly.modules.garbage_collection.utils;

import ee.aleksale.remindly.modules.garbage_collection.dto.GarbageCollectionSchedule;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class GarbageScheduleParserTest {

  @Test
  void shouldParseAllKnownGarbageTypesWithDates() {
    final var text = """
            Konteiner A | 0.24m3 biolagunevad köögi-ja sööklajäätmed - 1tk
            01.09.2026
            15.09.2026
            Konteiner B | 0.24m3 segaolmejäätmed - 1tk
            02.09.2026
            Konteiner C | 1.10m3 paber ja papp - 2tk
            03.09.2026
            17.09.2026
            """;

    final var result = GarbageScheduleParser.parse(text);

    assertEquals(3, result.size());
    assertEquals(GarbageCollectionSchedule.GarbageType.BIO, result.get(0).getType());
    assertEquals(List.of(LocalDate.of(2026, 9, 1), LocalDate.of(2026, 9, 15)), result.get(0).getDates());

    assertEquals(GarbageCollectionSchedule.GarbageType.MIXED, result.get(1).getType());
    assertEquals(List.of(LocalDate.of(2026, 9, 2)), result.get(1).getDates());

    assertEquals(GarbageCollectionSchedule.GarbageType.PACKAGING, result.get(2).getType());
    assertEquals(List.of(LocalDate.of(2026, 9, 3), LocalDate.of(2026, 9, 17)), result.get(2).getDates());
  }

  @Test
  void shouldReturnEmptyListWhenNoHeadersExist() {
    final var text = """
            01.09.2026
            15.09.2026
            """;

    final var result = GarbageScheduleParser.parse(text);

    assertEquals(List.of(), result);
  }

  @Test
  void shouldThrowWhenGarbageTypeIsUnknown() {
    final var text = """
            Konteiner X | 0.24m3 klaasijäätmed - 1tk
            01.09.2026
            """;

    final var exception = assertThrows(IllegalArgumentException.class, () -> GarbageScheduleParser.parse(text));

    assertEquals("Unknown garbage type: klaasijäätmed", exception.getMessage());
  }
}
