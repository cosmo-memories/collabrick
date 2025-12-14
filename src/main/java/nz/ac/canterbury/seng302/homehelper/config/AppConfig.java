package nz.ac.canterbury.seng302.homehelper.config;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * Component that provides access to the application's base URL as defined
 * in the application properties.
 */
@Component
public class AppConfig {

    /**
     * The base URL of the application injected from the app.base-url property
     * defined in the application properties.
     */
    @Value("${app.full-base-url}")
    private String fullBaseUrl;

    @Value("${app.base-url}")
    private String baseUrl;

    @Value("${app.ws-url}")
    private String webSocketUrl;

    public String getFullBaseUrl() {
        return fullBaseUrl;
    }

    /**
     * Returns the applications base URL as defined in the configuration.
     *
     * @return the configured base URL.
     */
    public String getBaseUrl() {
        return baseUrl;
    }

    /**
     * Returns the application WebSocket URL as defined in the configuration.
     *
     * @return the configured WebSocket URL.
     */
    public String getWebSocketUrl() {
        return webSocketUrl;
    }

    /**
     * Creates a UriComponentsBuilder initialised with the application's base URL,
     * the current request URI, and its query string.
     *
     * @param request the current HTTP request.
     * @return a UriComponentsBuilder representing the full request URI.
     */
    public UriComponentsBuilder buildUriFromRequest(HttpServletRequest request) {
        return UriComponentsBuilder
                .fromUriString(baseUrl)
                .path(request.getRequestURI())
                .query(request.getQueryString());
    }
}
