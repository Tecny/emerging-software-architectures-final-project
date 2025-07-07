package com.dtaquito_backend.dtaquito_backend.reservations.domain.services;

import com.dtaquito_backend.dtaquito_backend.reservations.domain.model.aggregates.Reservations;
import com.dtaquito_backend.dtaquito_backend.reservations.domain.model.queries.GetReservationsByUserIdQuery;

import java.util.List;


public interface ReservationsQueryService {

    List<Reservations> handle(GetReservationsByUserIdQuery query);
    List<Reservations> getAllReservations();
}
