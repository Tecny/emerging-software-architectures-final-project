package com.dtaquito_backend.dtaquito_backend.sportspaces.domain.model.queries;

public record GetSportSpacesByUserIdQuery(Long userId) {
    public GetSportSpacesByUserIdQuery {
        if (userId == null) {
            throw new IllegalArgumentException("UserId is required");
        }
    }
}
