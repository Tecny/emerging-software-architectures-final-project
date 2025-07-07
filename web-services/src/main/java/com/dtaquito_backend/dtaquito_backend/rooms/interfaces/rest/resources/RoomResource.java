package com.dtaquito_backend.dtaquito_backend.rooms.interfaces.rest.resources;

public record RoomResource(
        Long id,
        ReservationDTO reservation,
        String playerCount
) {
}
