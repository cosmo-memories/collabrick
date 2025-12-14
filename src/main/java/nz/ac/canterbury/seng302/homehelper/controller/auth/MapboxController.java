package nz.ac.canterbury.seng302.homehelper.controller.auth;

import jakarta.servlet.http.HttpServletRequest;
import nz.ac.canterbury.seng302.homehelper.service.MapboxService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

/**
 * Controller to handle requests to Map Box for location autocomplete
 * <p>
 * Code modified from chat gpt
 */
@RestController
public class MapboxController {

    private final MapboxService mapboxService;
    Logger logger = LoggerFactory.getLogger(MapboxController.class);

    @Autowired
    public MapboxController(MapboxService mapboxService) {
        this.mapboxService = mapboxService;
    }

    /**
     * Get mapping to autocomplete end point which returns json of the matching locations
     * to be displayed
     *
     * @param query string to query the MapBox API with
     * @return Mono<String> containing the location suggestions
     */
    @GetMapping("/autocomplete")
    public String autocomplete(@RequestParam String query, HttpServletRequest request) {
        String ip = extractClientIp(request);
        double lat = -43.5321; // Default Latitude for Christchurch
        double lon = 172.6362; // Default Longitude for Christchurch

        try {
            RestTemplate restTemplate = new RestTemplate();
            String geoUrl = "http://ip-api.com/json/" + ip;
            Map<String, Object> geoData = restTemplate.getForObject(geoUrl, Map.class);

            if (geoData != null && "success".equals(geoData.get("status"))) {
                lat = ((Number) geoData.get("lat")).doubleValue();
                lon = ((Number) geoData.get("lon")).doubleValue();
            }
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
        return mapboxService.autocomplete(query, lat, lon);
    }

    private String extractClientIp(HttpServletRequest request) {
        String testIp = request.getHeader("X-Test-IP");
        if (testIp != null && !testIp.isBlank()) {
            return testIp;
        }

        String forwarded = request.getHeader("X-Forwarded-For");
        return (forwarded != null && !forwarded.isEmpty())
                ? forwarded.split(",")[0]
                : request.getRemoteAddr();
    }

}
