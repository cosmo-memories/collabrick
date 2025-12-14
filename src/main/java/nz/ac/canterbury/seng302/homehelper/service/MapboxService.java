package nz.ac.canterbury.seng302.homehelper.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Service
public class MapboxService {

    @Value("${mapbox.access.token}")
    private String apiKey;

    private RestTemplate restTemplate = new RestTemplate();


    /**
     * Fetches locations that match the query from the MapBox API
     * <p>
     * This code is modified from Chat GPT
     *
     * @param query string to query the MapBox API with
     * @return JSON string response from Mapbox
     */
    public String autocomplete(String query, double lat, double lon) {
        try {
            String encodedQuery = URLEncoder.encode(query, StandardCharsets.UTF_8);
            String url = String.format(
                    "https://api.mapbox.com/search/geocode/v6/forward?q=%s&access_token=%s&limit=5&proximity=%f,%f",
                    encodedQuery, apiKey, lon, lat
            );

            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            return response.getBody();
        } catch (Exception e) {
            // Handle error logging and fallback here
            return "{\"error\":\"Request failed\"}";
        }
    }

    public void setRestTemplate(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }
}

