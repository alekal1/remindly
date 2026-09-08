package ee.aleksale.remindly.modules.snooze.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import ee.aleksale.remindly.core.client.NtfyClient;
import ee.aleksale.remindly.modules.snooze.dto.Snooze;
import ee.aleksale.remindly.modules.snooze.service.SnoozeService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;

@WebMvcTest(SnoozeController.class)
@Import(SnoozeControllerTest.SnoozeControllerConfiguration.class)
public class SnoozeControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @MockitoBean
  private SnoozeService snoozeService;

  private final ObjectMapper objectMapper = new ObjectMapper();

  @Test
  void shouldReturn400_whenParentEventIdMissing() throws Exception {
    final var request = Snooze.builder()
        .newScheduledAt(LocalDateTime.now())
        .build();

    mockMvc.perform(post("/v1/snooze")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest());

    verify(snoozeService, never()).snoozeEvent(any(Snooze.class));
  }

  @Test
  void shouldReturn400_whenNewScheduledAtMissing() throws Exception {
    final var request = Snooze.builder()
        .parentEventId(1L)
        .build();

    mockMvc.perform(post("/v1/snooze")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest());

    verify(snoozeService, never()).snoozeEvent(any(Snooze.class));
  }

  @Test
  void shouldReturn202_whenSnoozeIsCalled() throws Exception {
    final var request = Snooze.builder()
        .parentEventId(1L)
        .newScheduledAt(LocalDateTime.now())
        .build();

    mockMvc.perform(post("/v1/snooze")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isAccepted());

    verify(snoozeService).snoozeEvent(any(Snooze.class));
  }

  @TestConfiguration
  protected static class SnoozeControllerConfiguration {

    @Bean
    public NtfyClient client() {
      return mock(NtfyClient.class);
    }
  }
}
