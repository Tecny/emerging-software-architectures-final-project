package com.dtaquito_backend.dtaquito_backend.chat.interfaces.rest;

import com.dtaquito_backend.dtaquito_backend.chat.application.internal.commandservices.ChatRoomCommandServiceImpl;
import com.dtaquito_backend.dtaquito_backend.chat.domain.config.ChatWebSocketHandler;
import com.dtaquito_backend.dtaquito_backend.chat.domain.model.aggregates.ChatRoom;
import com.dtaquito_backend.dtaquito_backend.chat.domain.model.entities.Message;
import com.dtaquito_backend.dtaquito_backend.chat.domain.model.entities.MessageContentDTO;
import com.dtaquito_backend.dtaquito_backend.chat.domain.model.entities.MessageDTO;
import com.dtaquito_backend.dtaquito_backend.chat.domain.model.entities.UserDTO;
import com.dtaquito_backend.dtaquito_backend.iam.infrastructure.authorization.sfs.model.UserDetailsImpl;
import com.dtaquito_backend.dtaquito_backend.rooms.domain.model.aggregates.Rooms;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.swagger.v3.oas.annotations.Hidden;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.socket.TextMessage;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/chat")
public class ChatRoomController {

    private final ChatRoomCommandServiceImpl chatRoomService;
    private final ChatWebSocketHandler chatWebSocketHandler;

    public ChatRoomController(ChatRoomCommandServiceImpl chatRoomService, ChatWebSocketHandler chatWebSocketHandler) {
        this.chatRoomService = chatRoomService;
        this.chatWebSocketHandler = chatWebSocketHandler;
    }

    @Hidden
    @PostMapping("/rooms")
    public ResponseEntity<ChatRoom> createChatRoom(@RequestBody Rooms room) {
        try {
            ChatRoom chatRoom = chatRoomService.createChatRoom(room);
            return ResponseEntity.ok(chatRoom);
        }
        catch (ResourceAccessException e) {
            throw e;
        }
    }

    @PostMapping("/rooms/{chatRoomId}/messages")
    public ResponseEntity<MessageDTO> sendMessage(@PathVariable Long chatRoomId, @RequestBody MessageContentDTO messageContentDTO, Authentication authentication) {

        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        }
        UserDetailsImpl principal = (UserDetailsImpl) authentication.getPrincipal();
        Long userId = principal.getId();

        if (!chatRoomService.isUserInRoom(chatRoomId, userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
        }

        Message message = chatRoomService.sendMessage(chatRoomId, messageContentDTO.getContent(), userId);
        UserDTO userDTO = new UserDTO(message.getUser().getId(), message.getUser().getName());
        MessageDTO responseDTO = new MessageDTO(message.getContent(), message.getCreatedAt(), userDTO, chatRoomId);

        try {
            System.out.println("Enviando mensaje a través de WebSocket: " + responseDTO.getContent());

            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.registerModule(new JavaTimeModule());
            objectMapper.setTimeZone(java.util.TimeZone.getTimeZone("UTC"));
            objectMapper.configure(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);

            String jsonResponse = objectMapper.writeValueAsString(responseDTO);
            chatWebSocketHandler.broadcastMessage(new TextMessage(jsonResponse));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
        return ResponseEntity.ok(responseDTO);
    }

    @Hidden
    @GetMapping("/rooms/{chatRoomId}/messages")
    public ResponseEntity<List<MessageDTO>> getMessages(@PathVariable Long chatRoomId, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        UserDetailsImpl principal = (UserDetailsImpl) authentication.getPrincipal();
        Long userId = principal.getId();

        if (!chatRoomService.isUserInRoom(chatRoomId, userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        List<Message> messages = chatRoomService.getMessages(chatRoomId);
        List<MessageDTO> responseDTOs = messages.stream()
                .map(message -> {
                    UserDTO userDTO = new UserDTO(message.getUser().getId(), message.getUser().getName());
                    return new MessageDTO(message.getContent(), message.getCreatedAt(), userDTO, chatRoomId);
                })
                .collect(Collectors.toList());
        return ResponseEntity.ok(responseDTOs);
    }
}