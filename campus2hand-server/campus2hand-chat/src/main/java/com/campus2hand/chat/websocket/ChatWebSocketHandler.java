package com.campus2hand.chat.websocket;

import com.campus2hand.common.util.JwtUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.net.URI;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class ChatWebSocketHandler extends TextWebSocketHandler {

    private final JwtUtil jwtUtil;
    private static final Map<Long, WebSocketSession> userSessions = new ConcurrentHashMap<>();

    public ChatWebSocketHandler(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        URI uri = session.getUri();
        if (uri == null) {
            session.close(CloseStatus.BAD_DATA);
            return;
        }
        String query = uri.getQuery();
        String token = null;
        if (query != null) {
            for (String param : query.split("&")) {
                String[] pair = param.split("=", 2);
                if ("token".equals(pair[0]) && pair.length > 1) {
                    token = pair[1];
                    break;
                }
            }
        }
        if (token == null || !jwtUtil.validateAccessToken(token)) {
            session.close(CloseStatus.NOT_ACCEPTABLE);
            return;
        }
        Long userId = jwtUtil.getUserIdFromAccessToken(token);
        userSessions.put(userId, session);
        session.getAttributes().put("userId", userId);
        log.info("WebSocket connected: userId={}", userId);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        Long senderId = (Long) session.getAttributes().get("userId");
        log.info("WebSocket message from userId={}: {}", senderId, message.getPayload());
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        Long userId = (Long) session.getAttributes().get("userId");
        if (userId != null) {
            userSessions.remove(userId);
            log.info("WebSocket disconnected: userId={}", userId);
        }
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        log.error("WebSocket transport error", exception);
        session.close(CloseStatus.SERVER_ERROR);
    }

    public static WebSocketSession getSession(Long userId) {
        return userSessions.get(userId);
    }
}