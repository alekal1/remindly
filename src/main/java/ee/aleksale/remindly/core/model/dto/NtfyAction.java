package ee.aleksale.remindly.core.model.dto;

import lombok.Builder;
import lombok.NonNull;
import org.springframework.http.HttpMethod;

import java.util.Map;

@Builder
public class NtfyAction {
  @NonNull
  private NtfyActionType action;
  @NonNull
  private String label;
  @NonNull
  private String url;
  private HttpMethod method;
  private String body;
  private Map<String, String> headers;

  @Override
  public String toString() {
    var s = new StringBuilder(String.format(
            "%s, %s, %s",
            action.name().toLowerCase(),
            formatValue(label),
            formatValue(url)
    ));
    if (method != null) {
      s.append(", method=").append(method.name());
    }
    if (headers != null && !headers.isEmpty()) {
      headers.forEach((key, value) ->
              s.append(", headers.").append(key).append("=").append(formatValue(value))
      );
    }
    if (body != null) {
      s.append(", body=").append(formatValue(body));
    }
    return s.toString();
  }

  private static String formatValue(String value) {
    if (value == null || !requiresQuoting(value)) {
      return value;
    }

    if (!value.contains("'")) {
      return "'" + value + "'";
    }

    return "\"" + value
            .replace("\\", "\\\\")
            .replace("\"", "\\\"") + "\"";
  }

  private static boolean requiresQuoting(String value) {
    return value.contains(",") || value.contains(";") || value.contains("\"") || value.contains("'");
  }

  public enum NtfyActionType {
    HTTP
  }
}
