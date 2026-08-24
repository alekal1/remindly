package ee.aleksale.remindly.modules.garbage_collection.service;

import ee.aleksale.remindly.core.exception.RemindlyException;
import ee.aleksale.remindly.modules.garbage_collection.dto.GarbageCollectionSchedule;
import ee.aleksale.remindly.core.service.FileExtractorService;
import ee.aleksale.remindly.modules.garbage_collection.utils.GarbageScheduleParser;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;


@Slf4j
@Service
public class GarbageScheduleExtractorService implements FileExtractorService<List<GarbageCollectionSchedule>> {

  @Override
  public List<GarbageCollectionSchedule> extract(byte[] bytes) {
    try (final var document = Loader.loadPDF(bytes)) {
      final var stripper = new PDFTextStripper();
      final var pdfText = stripper.getText(document);

      return GarbageScheduleParser.parse(pdfText);
    } catch (IOException e) {
      log.error("Failed to extract garbage schedule from file: {}", e.getMessage());
      throw new RemindlyException(e.getMessage());
    }
  }
}
