package nz.ac.canterbury.seng302.homehelper.config.chat;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

/**
 * Intercepts the WebSocket handshake to extract and store the authenticated users ID. This ensures only authenticated
 * users can establish a WebSocket connection.
 */
@Component
public class WebSocketHandshakeInterceptor implements HandshakeInterceptor {

    private final Logger logger = LoggerFactory.getLogger(this.getClass().getName());

    /**
     * Called before the WebSocket handshake is completed. The authentication context is retrieved and if the user is
     * authenticated, the user's ID is stored in the session attributes.
     *
     * @param request    The HTTP request.
     * @param response   The HTTP response.
     * @param wsHandler  The WebSocket handler.
     * @param attributes The attributes to store for the WebSocket session.
     * @return true if the handshake should proceed, false to reject it.
     */
    @Override
    public boolean beforeHandshake(
            @NotNull ServerHttpRequest request,
            @NotNull ServerHttpResponse response,
            @NotNull WebSocketHandler wsHandler,
            @NotNull Map<String, Object> attributes
    ) {
        logger.debug("Attempting WebSocket handshake");

        SecurityContext securityContext = SecurityContextHolder.getContext();
        Authentication authentication = securityContext.getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            logger.debug("Handshake failed: No Authentication object found in SecurityContext.");
            return false;
        }

        Object principal = authentication.getPrincipal();
        if (!(principal instanceof Long userId)) {
            logger.debug("Handshake failed: Principal is not of expected type (Long). Actual: {}", principal.getClass().getName());
            return false;
        }

        logger.debug("Handshake successful: User ID {} added to WebSocket session attributes.", userId);
        attributes.put("userId", userId);
        return true;
    }

    /**
     * Called after the WebSocket handshake is completed.
     *
     * @param request   The HTTP request.
     * @param response  The HTTP response.
     * @param wsHandler The WebSocket handler.
     * @param exception An exception that occurred during handshake, if any.
     */
    @Override
    public void afterHandshake(
            @NotNull ServerHttpRequest request,
            @NotNull ServerHttpResponse response,
            @NotNull WebSocketHandler wsHandler,
            Exception exception
    ) {
        if (exception != null) {
            logger.warn("WebSocket handshake completed with exception: {}", exception.getMessage(), exception);
        } else {
            logger.debug("WebSocket handshake completed successfully.");
        }
    }
}
