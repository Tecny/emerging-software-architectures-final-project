package com.dtaquito_backend.dtaquito_backend.chat.infrastructure.persistance.jpa;

import com.dtaquito_backend.dtaquito_backend.chat.domain.model.aggregates.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {

    Optional<ChatRoom> findByRoomId(Long roomId);
    void deleteByRoomId(Long roomId);
}