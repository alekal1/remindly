package ee.aleksale.remindly.modules.garbage_collection.service;

import static org.junit.jupiter.api.Assertions.assertThrows;

import ee.aleksale.remindly.core.exception.RemindlyException;
import ee.aleksale.remindly.modules.garbage_collection.garbage.service.GarbageScheduleExtractorService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.stream.Stream;

@ExtendWith(MockitoExtension.class)
public class GarbageScheduledExtractorServiceTest {

  private GarbageScheduleExtractorService garbageScheduleExtractorService;

  @BeforeEach
  void init() {
    garbageScheduleExtractorService = new GarbageScheduleExtractorService();
  }


  private static Stream<Arguments> invalidBytes() {
    return Stream.of(
        Arguments.of("not a valid pdf".getBytes()),
        Arguments.of(new byte[0])
    );
  }

  @ParameterizedTest
  @MethodSource("invalidBytes")
  void shouldThrowRemindlyException_whenInvalidPdfBytesProvided(byte[] invalidPdfBytes) {
    assertThrows(RemindlyException.class, () -> garbageScheduleExtractorService.extract(invalidPdfBytes));
  }
}
