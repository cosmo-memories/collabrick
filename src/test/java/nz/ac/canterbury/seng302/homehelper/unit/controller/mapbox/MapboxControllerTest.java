package nz.ac.canterbury.seng302.homehelper.unit.controller.mapbox;

import nz.ac.canterbury.seng302.homehelper.service.MapboxService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@WithMockUser(username = "1")
public class MapboxControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RestTemplate restTemplate;

    @MockBean
    private MapboxService mapboxService;

    @Test
    public void testAutocompleteEndpointReturnsSuggestions() throws Exception {
        String query = "Christchurch";
        String mockResponse = "{\"suggestions\": [\"Christchurch Central\", \"Christchurch East\"]}";

        when(restTemplate.getForObject(anyString(), eq(Map.class)))
                .thenReturn(Map.of("status", "success", "lat", -43.5321, "lon", 172.6362));

        when(mapboxService.autocomplete(eq(query), anyDouble(), anyDouble())) .thenReturn(mockResponse);
        // Comparing exact doubles introduces floating point precisions errors

        mockMvc.perform(get("/autocomplete")
                        .param("query", query)
                        .header("X-Test-IP", "8.8.8.8")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string(mockResponse));
    }

    @Test
    public void testAutocompleteFallsBackWhenIpApiReturnsFailureStatus() throws Exception {
        String query = "Christchurch";
        String mockResponse = "{\"suggestions\": [\"Fallback\"]}";

        when(restTemplate.getForObject(anyString(), eq(Map.class)))
                .thenReturn(Map.of("status", "fail")); // e.g., blocked, bad IP

        when(mapboxService.autocomplete(eq(query), eq(-43.5321), eq(172.6362)))
                .thenReturn(mockResponse);

        mockMvc.perform(get("/autocomplete")
                        .param("query", query)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(mockResponse));
    }

}

