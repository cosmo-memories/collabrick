package nz.ac.canterbury.seng302.homehelper.config.chat;

import nz.ac.canterbury.seng302.homehelper.config.AppConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * Configuration for enabling and setting up WebSocket message handling using STOMP.
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final AppConfig appConfig;
    private final WebSocketHandshakeInterceptor handshakeInterceptor;

    /**
     * Constructs a new WebSocketConfig.
     *
     * @param appConfig            The application configuration for retrieving environment-specific settings.
     * @param handshakeInterceptor The interceptor to customize the WebSocket handshake process.
     */
    @Autowired
    public WebSocketConfig(AppConfig appConfig, WebSocketHandshakeInterceptor handshakeInterceptor) {
        this.appConfig = appConfig;
        this.handshakeInterceptor = handshakeInterceptor;
    }

    /**
     * Configures the message broker that will be used to route messages.
     * Enables a simple in-memory broker with the destination prefix /topic and sets the application destination prefix
     * to /app.
     *
     * @param config The message broker registry to configure.
     */
    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/topic");
        config.setApplicationDestinationPrefixes("/app");
    }

    /**
     * Registers a STOMP endpoint at /ws for WebSocket communication. The connection is established from this endpoint.
     *
     * @param registry The registry to add endpoints to.
     */
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws/chat")
                .addInterceptors(handshakeInterceptor)
                .setAllowedOrigins(appConfig.getBaseUrl());
    }
}