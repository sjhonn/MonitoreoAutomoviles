package com.autotrack.config;

import com.autotrack.websocket.AuthHandshakeInterceptor;
import com.autotrack.websocket.LocationWebSocketHandler;
import java.util.Arrays;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {
    private final LocationWebSocketHandler locationWebSocketHandler;
    private final AuthHandshakeInterceptor authHandshakeInterceptor;
    private final String[] allowedOriginPatterns;

    public WebSocketConfig(
            LocationWebSocketHandler locationWebSocketHandler,
            AuthHandshakeInterceptor authHandshakeInterceptor,
            @Value("${app.cors.allowed-origins:http://localhost:8080}") String allowedOrigins) {
        this.locationWebSocketHandler = locationWebSocketHandler;
        this.authHandshakeInterceptor = authHandshakeInterceptor;
        this.allowedOriginPatterns = Arrays.stream(allowedOrigins.split(","))
                .map(String::trim)
                .filter(value -> !value.isEmpty())
                .toArray(String[]::new);
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(locationWebSocketHandler, "/ws/locations")
                .addInterceptors(authHandshakeInterceptor)
                .setAllowedOriginPatterns(allowedOriginPatterns);
    }
}
