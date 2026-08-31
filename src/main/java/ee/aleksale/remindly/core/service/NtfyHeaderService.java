package ee.aleksale.remindly.core.service;

import ee.aleksale.remindly.core.model.dto.NtfyAction;
import ee.aleksale.remindly.core.model.dto.NtfyRequestPayload;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.stream.Collectors;

@Slf4j
@Service
public class NtfyHeaderService {
  private static final String NTFY_EMOJIS_HEADER = "Tags";
  private static final String NTFY_TITLE_HEADER = "Title";
  private static final String NTFY_ACTIONS_HEADER = "Actions";


  public HttpHeaders getHeaders(NtfyRequestPayload payload) {
    var headers = new HttpHeaders();
    if (payload == null) {
      log.info("No payload provided for headers");
      return headers;
    }

    if (StringUtils.hasLength(payload.emojis())) {
      headers.add(NTFY_EMOJIS_HEADER, payload.emojis());
    }

    if (StringUtils.hasLength(payload.title())) {
      headers.add(NTFY_TITLE_HEADER, payload.title());
    }

    if (payload.ntfyActions() != null && !payload.ntfyActions().isEmpty()) {
      headers.add(NTFY_ACTIONS_HEADER,
              payload.ntfyActions()
                      .stream()
                      .map(NtfyAction::toString)
                      .collect(Collectors.joining(";")));
    }

    return headers;
  }

}
