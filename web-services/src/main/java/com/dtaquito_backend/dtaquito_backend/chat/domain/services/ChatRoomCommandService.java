package com.dtaquito_backend.dtaquito_backend.chat.domain.services;

import com.dtaquito_backend.dtaquito_backend.chat.domain.model.aggregates.ChatRoom;
import com.dtaquito_backend.dtaquito_backend.chat.domain.model.entities.Message;
import com.dtaquito_backend.dtaquito_backend.rooms.domain.model.aggregates.Rooms;

import java.util.List;

public interface ChatRoomCommandService {

    ChatRoom createChatRoom(Rooms room);

    Message sendMessage(Long chatRoomId, String content, Long userId);

    List<Message> getMessages(Long chatRoomId);

}
