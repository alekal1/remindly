package ee.aleksale.remindly.modules.garbage_collection.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import ee.aleksale.remindly.core.client.NtfyClient;
import ee.aleksale.remindly.modules.garbage_collection.garbage.controller.GarbageCollectionController;
import ee.aleksale.remindly.modules.garbage_collection.garbage.service.GarbageCollectionService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.multipart.MultipartFile;

@WebMvcTest(GarbageCollectionController.class)
@Import(GarbageCollectionControllerTest.GarbageCollectionControllerConfiguration.class)
public class GarbageCollectionControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @MockitoBean
  private GarbageCollectionService garbageCollectionService;

  @Test
  void shouldReturn202_andCallService_whenFileProvided() throws Exception {
    final var file = new MockMultipartFile(
        "file",
        "schedule.pdf",
        "application/pdf",
        "pdf-bytes".getBytes()
    );
    final var fileCaptor = ArgumentCaptor.forClass(MultipartFile.class);

    mockMvc.perform(multipart("/v1/garbage-collection/reset").file(file))
        .andExpect(status().isAccepted());

    verify(garbageCollectionService).resetAndExtractSchedules(fileCaptor.capture());

    final var capturedFile = fileCaptor.getValue();
    assertEquals("schedule.pdf", capturedFile.getOriginalFilename());
    assertEquals("application/pdf", capturedFile.getContentType());
  }

  @Test
  void shouldReturn400_whenFileMissing() throws Exception {
    mockMvc.perform(multipart("/v1/garbage-collection/reset"))
        .andExpect(status().isBadRequest());

    verify(garbageCollectionService, never()).resetAndExtractSchedules(any());
  }

  @Test
  void shouldReturn202_andCallServiceWithDefaultLimit_whenLimitNotProvided() throws Exception {
    mockMvc.perform(get("/v1/garbage-collection"))
        .andExpect(status().isAccepted());

    verify(garbageCollectionService).notifyNextEvents(3);
  }

  @Test
  void shouldReturn202_andCallServiceWithProvidedLimit_whenLimitGiven() throws Exception {
    mockMvc.perform(get("/v1/garbage-collection").param("limit", "5"))
        .andExpect(status().isAccepted());

    verify(garbageCollectionService).notifyNextEvents(5);
  }

  @TestConfiguration
  protected static class GarbageCollectionControllerConfiguration {

    @Bean
    public NtfyClient client() {
      return mock(NtfyClient.class);
    }
  }
}
