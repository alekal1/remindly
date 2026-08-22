package ee.aleksale.remindly.garbage_collection.controller;

import ee.aleksale.remindly.garbage_collection.service.GarbageCollectionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/v1/garbage-collection")
public class GarbageCollectionController {

  private final GarbageCollectionService garbageCollectionService;

  @PostMapping(value = "/reset")
  public ResponseEntity<?> reset(@RequestParam(name = "file") MultipartFile garbageCollectionScheduleFile) {
    garbageCollectionService.resetAndExtractSchedules(garbageCollectionScheduleFile);
    return ResponseEntity.accepted().build();
  }

}
