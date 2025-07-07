package com.dtaquito_backend.dtaquito_backend.reservations.interfaces.rest.transform;

import com.dtaquito_backend.dtaquito_backend.reservations.domain.model.aggregates.Reservations;
import com.dtaquito_backend.dtaquito_backend.reservations.interfaces.rest.resources.ReservationsResource;

public class ReservationsResourceFromEntityAssembler {

    public static ReservationsResource toResourceFromEntity(Reservations entity) {
        return new ReservationsResource(entity.getId(), entity.getGameDay(), entity.getStartTime(), entity.getEndTime(), entity.getUser().getId(), entity.getSportSpaces().getId(), entity.getStatus(), entity.getType(), entity.getReservationName());
    }
}
