package com.dtaquito_backend.dtaquito_backend.rooms.domain.model.commands;

public record CreateRoomCommand(
        Long reservationId,
        Long playerListId
) {
}