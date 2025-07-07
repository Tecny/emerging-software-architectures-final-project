package com.dtaquito_backend.dtaquito_backend.chat.domain.model.entities;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Setter
@Getter
public class MessageDTO {
    private String content;
    private LocalDateTime createdAt;
    private UserDTO user;
    private Long roomId;

    public MessageDTO(String content, LocalDateTime createdAt, UserDTO user, Long roomId) {
        this.content = content;
        this.createdAt = createdAt;
        this.user = user;
        this.roomId = roomId;
    }
}