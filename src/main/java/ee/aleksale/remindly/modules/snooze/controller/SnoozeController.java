package ee.aleksale.remindly.modules.snooze.controller;

import ee.aleksale.remindly.core.constants.ApiConstants;
import ee.aleksale.remindly.modules.snooze.dto.Snooze;
import ee.aleksale.remindly.modules.snooze.service.SnoozeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping(value = ApiConstants.SNOOZE_API_URL)
public class SnoozeController {

  private final SnoozeService snoozeService;

  @PostMapping
  public ResponseEntity<Void> snooze(@Valid @RequestBody Snooze request) {
    snoozeService.snoozeEvent(request);
    return ResponseEntity.accepted().build();
  }

}
