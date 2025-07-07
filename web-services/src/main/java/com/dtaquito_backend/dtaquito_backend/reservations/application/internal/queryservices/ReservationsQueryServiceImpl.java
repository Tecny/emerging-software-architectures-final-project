package com.dtaquito_backend.dtaquito_backend.reservations.application.internal.queryservices;

import com.dtaquito_backend.dtaquito_backend.reservations.domain.model.aggregates.Reservations;
import com.dtaquito_backend.dtaquito_backend.reservations.domain.model.queries.GetReservationsByUserIdQuery;
import com.dtaquito_backend.dtaquito_backend.reservations.domain.services.ReservationsQueryService;
import com.dtaquito_backend.dtaquito_backend.reservations.infrastructure.persistance.jpa.ReservationsRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ReservationsQueryServiceImpl implements ReservationsQueryService {

    private final ReservationsRepository reservationsRepository;

    public ReservationsQueryServiceImpl(ReservationsRepository reservationsRepository) {
        this.reservationsRepository = reservationsRepository;

    }

    @Override
    public List<Reservations> handle(GetReservationsByUserIdQuery query){

        return reservationsRepository.findByUserId(query.userId());
    }

    @Override
    public List<Reservations> getAllReservations(){
        return reservationsRepository.findAll();
    }
}
