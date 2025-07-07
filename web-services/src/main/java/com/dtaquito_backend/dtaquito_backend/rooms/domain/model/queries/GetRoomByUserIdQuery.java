package com.dtaquito_backend.dtaquito_backend.rooms.domain.model.queries;

public record GetRoomByUserIdQuery(Long userId) {
    public GetRoomByUserIdQuery {
        if (userId == null) {
            throw new IllegalArgumentException("UserId is required");
        }
    }
}
