package ee.aleksale.remindly.modules.adhoc.controller;

import ee.aleksale.remindly.core.annotation.ReminderEnabled;
import ee.aleksale.remindly.core.constants.ApiConstants;
import ee.aleksale.remindly.core.model.type.ReminderType;
import ee.aleksale.remindly.modules.adhoc.dto.Adhoc;
import ee.aleksale.remindly.modules.adhoc.service.AdhocService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping(value = ApiConstants.ADHOC_API_URL)
public class AdhocController {

  private final AdhocService adhocService;

  @PostMapping
  @ReminderEnabled(type = ReminderType.ADHOC)
  public ResponseEntity<Void> scheduleAdhoc(@RequestBody @Valid Adhoc request) {
    adhocService.adhoc(request);

    return ResponseEntity.accepted().build();
  }

}
