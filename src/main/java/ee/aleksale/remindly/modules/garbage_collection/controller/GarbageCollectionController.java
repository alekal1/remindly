package ee.aleksale.remindly.modules.garbage_collection.controller;

import ee.aleksale.remindly.core.constants.ApiConstants;
import ee.aleksale.remindly.modules.garbage_collection.service.GarbageCollectionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping(value = ApiConstants.GARBAGE_COLLECTION_API_URL)
public class GarbageCollectionController {

  private final GarbageCollectionService garbageCollectionService;

  @PostMapping(value = "/reset")
  public ResponseEntity<Void> reset(@RequestParam(name = "file") MultipartFile garbageCollectionScheduleFile) {
    garbageCollectionService.resetAndExtractSchedules(garbageCollectionScheduleFile);
    return ResponseEntity.accepted().build();
  }

  @GetMapping
  public ResponseEntity<Void> notifyNextEvent(@RequestParam(name = "limit", defaultValue = "3") int limit) {
    garbageCollectionService.notifyNextEvents(limit);
    return ResponseEntity.accepted().build();
  }

}
