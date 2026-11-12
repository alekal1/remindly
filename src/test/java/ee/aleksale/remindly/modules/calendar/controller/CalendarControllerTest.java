package ee.aleksale.remindly.modules.calendar.controller;

import ee.aleksale.remindly.core.client.NtfyClient;
import ee.aleksale.remindly.modules.calendar.property.CalendarProperties;
import ee.aleksale.remindly.modules.calendar.service.CalendarFeedService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CalendarController.class)
@Import(CalendarControllerTest.CalendarControllerConfiguration.class)
class CalendarControllerTest {

  private static final String TOKEN = "secret-token";

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private CalendarProperties properties;

  @MockitoBean
  private CalendarFeedService calendarFeedService;

  @BeforeEach
  void init() {
    properties.setFeedToken(TOKEN);
  }

  @Test
  void shouldReturnFeed_whenTokenMatches() throws Exception {
    doReturn("BEGIN:VCALENDAR\r\nEND:VCALENDAR\r\n").when(calendarFeedService).renderFeed();

    mockMvc.perform(get("/v1/calendar/" + TOKEN + ".ics"))
            .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith("text/calendar"))
            .andExpect(header().string("Cache-Control", "no-cache"))
            .andExpect(content().string("BEGIN:VCALENDAR\r\nEND:VCALENDAR\r\n"));
  }

  @Test
  void shouldReturn404_whenTokenDoesNotMatch() throws Exception {
    mockMvc.perform(get("/v1/calendar/wrong-token.ics"))
            .andExpect(status().isNotFound());

    verify(calendarFeedService, never()).renderFeed();
  }

  @Test
  void shouldReturn404_whenFeedTokenNotConfigured() throws Exception {
    properties.setFeedToken("");

    mockMvc.perform(get("/v1/calendar/.ics"))
            .andExpect(status().isNotFound());

    verify(calendarFeedService, never()).renderFeed();
  }

  @TestConfiguration
  protected static class CalendarControllerConfiguration {

    @Bean
    public NtfyClient client() {
      return mock(NtfyClient.class);
    }

    @Bean
    public CalendarProperties calendarProperties() {
      return new CalendarProperties();
    }
  }
}
