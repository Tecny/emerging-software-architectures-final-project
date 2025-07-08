package com.dtaquito_backend.dtaquito_backend.rooms.interfaces.rest.transform;

import com.dtaquito_backend.dtaquito_backend.rooms.domain.model.aggregates.Rooms;
import com.dtaquito_backend.dtaquito_backend.rooms.interfaces.rest.resources.*;

public class RoomsResourceFromEntityAssembler {

    public static RoomResource toResourceFromEntity(Rooms room) {

        var sportSpace = room.getReservations().getSportSpaces();
        SportSpaceDTO sportSpaceDTO = new SportSpaceDTO(
                sportSpace.getId(),
                sportSpace.getName(),
                sportSpace.getImage(),
                sportSpace.getAddress(),
                sportSpace.getSport().getSportType(),
                sportSpace.getGame().getGameMode(),
                sportSpace.getPrice(),
                sportSpace.getAmount()
        );

        ReservationDTO reservationDTO = new ReservationDTO(
                room.getReservations().getId(),
                room.getReservations().getReservationName(),
                room.getReservations().getGameDay(),
                room.getReservations().getStartTime(),
                room.getReservations().getEndTime(),
                room.getReservations().getUser().getId(),
                room.getReservations().getUser().getName(),
                sportSpaceDTO
        );

        int currentPlayers = room.getPlayerLists().size();
        int maxPlayers = sportSpace.getGame().getGameMode().getMaxPlayers();
        String playerCount = currentPlayers + "/" + maxPlayers;

        return new RoomResource(
                room.getId(),
                reservationDTO,
                playerCount
        );
    }
}