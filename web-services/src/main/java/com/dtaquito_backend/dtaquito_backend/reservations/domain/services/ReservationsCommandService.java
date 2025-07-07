package com.dtaquito_backend.dtaquito_backend.reservations.domain.services;

import com.dtaquito_backend.dtaquito_backend.reservations.domain.model.aggregates.Reservations;
import com.dtaquito_backend.dtaquito_backend.reservations.domain.model.commands.CreateReservationsCommand;
import com.dtaquito_backend.dtaquito_backend.reservations.domain.model.events.ReservationCreatedEvent;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.Optional;

public interface ReservationsCommandService {

    Optional<Reservations> handle(Long id, CreateReservationsCommand command);
    void handleReservationsCreatedEvent(ReservationCreatedEvent event);

    @Scheduled(fixedRate = 60000)
    void deleteCommunityReservationsByOneHourBefore();

    @Scheduled(fixedRate = 60000)
    void deletePersonalReservationByEndTimeConcluded();
}
