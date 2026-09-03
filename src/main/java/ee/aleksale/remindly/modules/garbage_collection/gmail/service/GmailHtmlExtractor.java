package ee.aleksale.remindly.modules.garbage_collection.gmail.service;

import com.google.api.services.gmail.model.Message;
import com.google.api.services.gmail.model.MessagePart;
import ee.aleksale.remindly.core.service.ExtractorService;
import ee.aleksale.remindly.modules.garbage_collection.garbage.dto.GarbageCollectionSchedule;
import ee.aleksale.remindly.utils.DateUtils;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.springframework.stereotype.Service;
import org.springframework.util.MimeType;
import org.springframework.util.MimeTypeUtils;

import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Locale;

@Slf4j
@Service
public class GmailHtmlExtractor implements ExtractorService<List<GarbageCollectionSchedule>, Message> {

  @Override
  public List<GarbageCollectionSchedule> extract(Message data) {
    final var html = extractByMime(data.getPayload(), MimeTypeUtils.TEXT_HTML);

    final var doc = Jsoup.parse(html);
    final var table = doc.select("table:has(th:matchesOwn(^\\s*Jäätmeliik\\s*$))").first();
    if (table == null) {
      return List.of();
    }

    final var headers = table.select("thead th");
    int typeIdx = -1;
    int dateIdx = -1;

    for (int i = 0; i < headers.size(); i++) {
      String h = headers.get(i).text().trim();
      if (h.equals("Jäätmeliik")) typeIdx = i;
      if (h.equals("Tühjendamise kuupäev")) dateIdx = i;
    }
    if (typeIdx < 0 || dateIdx < 0) {
      return List.of();
    }

    List<GarbageCollectionSchedule> result = new ArrayList<>();
    for (var tr : table.select("tbody tr")) {
      var tds = tr.select("td");
      if (tds.size() > Math.max(typeIdx, dateIdx)) {
        result.add(
                GarbageCollectionSchedule.builder()
                        .type(GarbageCollectionSchedule.GarbageType.mapFromString(tds.get(typeIdx).text().trim().toLowerCase(Locale.ROOT)))
                        .dates(List.of(DateUtils.convertToLocalDate(
                                tds.get(dateIdx).text().trim().replace(" ", ""),
                                "dd.MM.yyyy")))
                        .build()
        );
      }
    }

    return result;
  }

  private static String extractByMime(MessagePart part, MimeType mimeType) {
    if (part == null) {
      return null;
    }

    if (mimeType.equals(MimeType.valueOf(part.getMimeType()))
            && part.getBody() != null
            && part.getBody().getData() != null) {
      byte[] decoded = Base64.getUrlDecoder().decode(part.getBody().getData());
      return new String(decoded);
    }

    if (part.getParts() != null) {
      for (MessagePart p : part.getParts()) {
        String result = extractByMime(p, mimeType);
        if (result != null) {
          return result;
        }
      }
    }
    return null;
  }
}
