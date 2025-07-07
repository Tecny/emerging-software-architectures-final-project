package com.dtaquito_backend.dtaquito_backend.reservations.domain.model.queries;

public record GetReservationsByUserIdQuery(Long userId) {
    public GetReservationsByUserIdQuery {
        if (userId == null) {
            throw new IllegalArgumentException("UserId is required");
        }
    }
}
