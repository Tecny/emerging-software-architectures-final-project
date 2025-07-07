package com.dtaquito_backend.dtaquito_backend.rooms.interfaces.rest.resources;

public record ReservationDTO (
        Long id,
        String reservationName,
        String gameDay,
        String startTime,
        String endTime,
        Long userId,
        String userName,
        SportSpaceDTO sportSpace
    ){
}
