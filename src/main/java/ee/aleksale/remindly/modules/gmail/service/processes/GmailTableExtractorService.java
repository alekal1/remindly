package ee.aleksale.remindly.modules.gmail.service.processes;

import com.google.api.services.gmail.model.MessagePart;
import ee.aleksale.remindly.core.service.ExtractorService;
import ee.aleksale.remindly.modules.gmail.model.GmailContext;
import org.jsoup.Jsoup;
import org.jsoup.select.Elements;
import org.springframework.util.MimeType;
import org.springframework.util.MimeTypeUtils;

import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public interface GmailTableExtractorService<RESULT> extends ExtractorService<RESULT, GmailTableExtractorService.GmailTableRowContext> {

  default List<RESULT> extractFromTable(GmailContext data) {
    final var tableConfig = data.template().getSelector().getTable();

    final var html = extractByMime(data.gmailMessage().getPayload(), MimeTypeUtils.TEXT_HTML);
    if (html == null) {
      return null;
    }

    final var doc = Jsoup.parse(html);
    final var table = doc.select(tableConfig.getAnchor()).first();

    if (table == null) {
      return null;
    }

    final var headers = table.select("thead th");

    Map<String, Integer> columnIndexes = new HashMap<>();

    for (var entry : tableConfig.getColumns().entrySet()) {
      columnIndexes.put(
              entry.getKey(),
              findColumnIndex(headers, entry.getValue())
      );
    }

    if (columnIndexes.values().stream().anyMatch(index -> index < 0)) {
      return null;
    }

    List<RESULT> result = new ArrayList<>();

    for (var tr : table.select("tbody tr")) {
      final var tds = tr.select("td");

      final int maxIndex = columnIndexes.values()
              .stream()
              .max(Integer::compareTo)
              .orElse(-1);

      if (tds.size() <= maxIndex) {
        continue;
      }

      Map<String, String> cols = new HashMap<>();

      for (var entry : columnIndexes.entrySet()) {
        cols.put(entry.getKey(), tds.get(entry.getValue()).text().trim());
      }

      result.add(extract(new GmailTableRowContext(data, cols)));
    }

    return result;
  }


  private int findColumnIndex(Elements headers, String expectedHeader) {
    for (int i = 0; i < headers.size(); i++) {
      String actualHeader = headers.get(i).text().trim();

      if (expectedHeader.equals(actualHeader)) {
        return i;
      }
    }

    return -1;
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

  record GmailTableRowContext(
          GmailContext context,
          Map<String, String> values
  ) {}

}
