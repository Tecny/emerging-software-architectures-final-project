package com.dtaquito_backend.dtaquito_backend.chat.application.internal.commandservices;

import com.dtaquito_backend.dtaquito_backend.chat.domain.model.aggregates.ChatRoom;
import com.dtaquito_backend.dtaquito_backend.chat.domain.model.entities.Message;
import com.dtaquito_backend.dtaquito_backend.chat.domain.services.ChatRoomCommandService;
import com.dtaquito_backend.dtaquito_backend.chat.infrastructure.persistance.jpa.ChatRoomRepository;
import com.dtaquito_backend.dtaquito_backend.chat.infrastructure.persistance.jpa.MessageRepository;
import com.dtaquito_backend.dtaquito_backend.player_list.domain.model.aggregates.PlayerList;
import com.dtaquito_backend.dtaquito_backend.users.domain.model.aggregates.User;
import com.dtaquito_backend.dtaquito_backend.users.infrastructure.persistance.jpa.UserRepository;
import com.dtaquito_backend.dtaquito_backend.rooms.domain.model.aggregates.Rooms;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ChatRoomCommandServiceImpl implements ChatRoomCommandService {

    private static final Logger logger = LoggerFactory.getLogger(ChatRoomCommandServiceImpl.class);

    private final ChatRoomRepository chatRoomRepository;
    private final UserRepository userRepository;
    private final MessageRepository messageRepository;

    public ChatRoomCommandServiceImpl(ChatRoomRepository chatRoomRepository, UserRepository userRepository, MessageRepository messageRepository) {
        this.chatRoomRepository = chatRoomRepository;
        this.userRepository = userRepository;
        this.messageRepository = messageRepository;
    }

    @Override
    public ChatRoom createChatRoom(Rooms room) {
        ChatRoom chatRoom = new ChatRoom(room);
        chatRoom.setName(room.getReservations().getReservationName());

        // Add the creator of the room to the chat room
        User creator = room.getReservations().getUser();
        chatRoom.addPlayer(creator);

        for (PlayerList playerList : room.getPlayerLists()) {
            chatRoom.addPlayer(playerList.getUser());
        }

        return chatRoomRepository.save(chatRoom);
    }

    // ChatRoomCommandServiceImpl.java
    public boolean isUserInRoom(Long chatRoomId, Long userId) {
        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId).orElse(null);
        if (chatRoom == null) {
            return false;
        }
        return chatRoom.getPlayerLists().stream()
                .anyMatch(playerList -> playerList.getUser().getId().equals(userId));
    }

    @Override
    public Message sendMessage(Long chatRoomId, String content, Long userId) {
        logger.debug("Attempting to send message to ChatRoom with id: {}", chatRoomId);

        Optional<ChatRoom> chatRoomOptional = chatRoomRepository.findById(chatRoomId);
        if (chatRoomOptional.isEmpty()) {
            logger.error("ChatRoom with id {} not found", chatRoomId);
            throw new IllegalArgumentException("ChatRoom not found");
        }

        ChatRoom chatRoom = chatRoomOptional.get();
        Message message = new Message();
        message.setContent(content);

        logger.debug("Attempting to find User with id: {}", userId);
        Optional<User> userOptional = userRepository.findById(userId);
        if (userOptional.isEmpty()) {
            logger.error("User with id {} not found", userId);
            throw new IllegalArgumentException("User not found");
        }

        User user = userOptional.get();
        message.setUser(user);
        chatRoom.addMessage(message);
        chatRoomRepository.save(chatRoom);

        logger.debug("Message sent successfully to ChatRoom with id: {}", chatRoomId);
        return message;
    }

    @Override
    public List<Message> getMessages(Long chatRoomId) {
        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
                .orElseThrow(() -> new IllegalArgumentException("ChatRoom not found"));
        return messageRepository.findByChatRoom(chatRoom);
    }
}