package com.autotrack.websocket;

import com.autotrack.dto.LocationResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.ConcurrentWebSocketSessionDecorator;
import org.springframework.web.socket.handler.TextWebSocketHandler;

@Component
public class LocationWebSocketHandler extends TextWebSocketHandler {
    private final ObjectMapper objectMapper;
    private final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();

    public LocationWebSocketHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        sessions.put(session.getId(), new ConcurrentWebSocketSessionDecorator(session, 5000, 512 * 1024));
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        sessions.remove(session.getId());
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        sessions.remove(session.getId());
        if (session.isOpen()) {
            session.close(CloseStatus.SERVER_ERROR);
        }
    }

    public void broadcast(LocationResponse location) {
        try {
            String payload = objectMapper.writeValueAsString(Map.of(
                    "type", "vehicle.location",
                    "data", location));
            TextMessage message = new TextMessage(payload);
            sessions.forEach((id, session) -> sendOrRemove(id, session, message));
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("Could not serialize WebSocket event.", ex);
        }
    }

    private void sendOrRemove(String id, WebSocketSession session, TextMessage message) {
        if (!session.isOpen()) {
            sessions.remove(id);
            return;
        }
        try {
            session.sendMessage(message);
        } catch (IOException ex) {
            sessions.remove(id);
            try {
                session.close(CloseStatus.SERVER_ERROR);
            } catch (IOException ignored) {
                // Session is already unusable.
            }
        }
    }
}
