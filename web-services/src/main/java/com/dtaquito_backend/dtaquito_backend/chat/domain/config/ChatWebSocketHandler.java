package com.dtaquito_backend.dtaquito_backend.chat.domain.config;

import com.dtaquito_backend.dtaquito_backend.rooms.domain.model.aggregates.Rooms;
import com.dtaquito_backend.dtaquito_backend.rooms.infrastructure.persistance.jpa.RoomsRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.ArrayList;
import java.util.List;

public class ChatWebSocketHandler extends TextWebSocketHandler {

    private final List<WebSocketSession> sessions = new ArrayList<>();
    private final RoomsRepository roomsRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public ChatWebSocketHandler(RoomsRepository roomsRepository) {
        this.roomsRepository = roomsRepository;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        sessions.add(session);
    }

    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String payload = message.getPayload();
        try {
            JsonNode jsonNode = objectMapper.readTree(payload);
            String roomId = extractRoomId(jsonNode);
            String userId = extractUserId(jsonNode);

            if (isUserInRoom(userId, roomId)) {
                super.handleTextMessage(session, message);
            } else {
                session.close();
            }
        } catch (Exception e) {
            session.sendMessage(new TextMessage("Invalid JSON format: " + e.getMessage()));
        }
    }

    private String extractRoomId(JsonNode jsonNode) {
        return jsonNode.get("roomId").asText();
    }

    private String extractUserId(JsonNode jsonNode) {
        return jsonNode.get("userId").asText();
    }

    private boolean isUserInRoom(String userId, String roomId) {
        Rooms room = roomsRepository.findById(Long.parseLong(roomId)).orElse(null);
        if (room == null) {
            return false;
        }
        return room.getPlayerLists().stream()
                .anyMatch(playerList -> playerList.getUser().getId().equals(Long.parseLong(userId)));
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        sessions.remove(session);
    }

    public void broadcastMessage(TextMessage message) throws Exception {
        for (WebSocketSession webSocketSession : sessions) {
            if (webSocketSession != null && webSocketSession.isOpen()) {
                webSocketSession.sendMessage(message);
            }
        }
    }
}