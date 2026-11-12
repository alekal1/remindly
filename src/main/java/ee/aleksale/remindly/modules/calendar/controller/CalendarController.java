package ee.aleksale.remindly.modules.calendar.controller;

import ee.aleksale.remindly.core.constants.ApiConstants;
import ee.aleksale.remindly.modules.calendar.property.CalendarProperties;
import ee.aleksale.remindly.modules.calendar.service.CalendarFeedService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

@RestController
@RequiredArgsConstructor
@RequestMapping(value = ApiConstants.CALENDAR_API_URL)
public class CalendarController {

  private final CalendarFeedService calendarFeedService;
  private final CalendarProperties properties;

  @GetMapping("/{token}.ics")
  public ResponseEntity<String> feed(@PathVariable("token") String token) {
    if (!isValidToken(token)) {
      return ResponseEntity.notFound().build();
    }

    return ResponseEntity.ok()
            .contentType(new MediaType("text", "calendar", StandardCharsets.UTF_8))
            .cacheControl(CacheControl.noCache())
            .body(calendarFeedService.renderFeed());
  }

  private boolean isValidToken(String token) {
    final var expected = properties.getFeedToken();
    if (!StringUtils.hasText(expected) || !StringUtils.hasText(token)) {
      return false;
    }
    return MessageDigest.isEqual(
            expected.getBytes(StandardCharsets.UTF_8),
            token.getBytes(StandardCharsets.UTF_8));
  }
}
