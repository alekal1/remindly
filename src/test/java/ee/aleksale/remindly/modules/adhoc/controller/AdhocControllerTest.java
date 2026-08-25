package ee.aleksale.remindly.modules.adhoc.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import ee.aleksale.remindly.core.client.NtfyClient;
import ee.aleksale.remindly.modules.adhoc.dto.Adhoc;
import ee.aleksale.remindly.modules.adhoc.service.AdhocService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
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
import java.util.stream.Stream;

@WebMvcTest(AdhocController.class)
@Import(AdhocControllerTest.AdhocControllerConfiguration.class)
public class AdhocControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @MockitoBean
  private AdhocService adhocService;

  private final ObjectMapper objectMapper = new ObjectMapper();

  private static Stream<Arguments> blankValues(){
    return Stream.of(
            null,
            Arguments.of(""),
            Arguments.of(" "),
            Arguments.of("  "),
            Arguments.of("   ")
    );
  }

  @ParameterizedTest
  @MethodSource("blankValues")
  void shouldReturn400_whenInvalidMessage(String message) throws Exception {
    final var request = Adhoc.builder()
            .scheduledAt(LocalDateTime.now())
            .message(message)
            .build();

    mockMvc.perform(post("/v1/adhoc")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest());

    verify(adhocService, never()).adhoc(request);
  }

  @Test
  void shouldReturn400_whenScheduledAtInvalid() throws Exception {
    final var request = Adhoc.builder()
            .scheduledAt(null)
            .message("any")
            .build();

    mockMvc.perform(post("/v1/adhoc")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest());

    verify(adhocService, never()).adhoc(request);
  }

  @Test
  void shouldReturn202_whenAdhocIsCalled() throws Exception {
    final var request = Adhoc.builder()
            .scheduledAt(LocalDateTime.now())
            .message("any")
            .build();

    mockMvc.perform(post("/v1/adhoc")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isAccepted());

    verify(adhocService).adhoc(any(Adhoc.class));
  }


  @TestConfiguration
  protected static class AdhocControllerConfiguration {

    @Bean
    public NtfyClient client() {
      return mock(NtfyClient.class);
    }

  }
}
