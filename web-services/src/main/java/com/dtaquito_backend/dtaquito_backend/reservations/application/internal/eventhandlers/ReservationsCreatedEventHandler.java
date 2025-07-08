package com.dtaquito_backend.dtaquito_backend.reservations.application.internal.eventhandlers;

import com.dtaquito_backend.dtaquito_backend.reservations.domain.model.events.ReservationCreatedEvent;
import com.dtaquito_backend.dtaquito_backend.reservations.domain.model.queries.GetReservationsByUserIdQuery;
import com.dtaquito_backend.dtaquito_backend.reservations.domain.services.ReservationsCommandService;
import com.dtaquito_backend.dtaquito_backend.reservations.domain.services.ReservationsQueryService;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

@Service
public class ReservationsCreatedEventHandler {

    private final ReservationsCommandService reservationsCommandService;
    private final ReservationsQueryService reservationsQueryService;

    public ReservationsCreatedEventHandler(ReservationsCommandService reservationsCommandService, ReservationsQueryService reservationsQueryService) {
        this.reservationsCommandService = reservationsCommandService;
        this.reservationsQueryService = reservationsQueryService;
    }

    @EventListener(ReservationCreatedEvent.class)
    public void on(ReservationCreatedEvent event){
        System.out.println("ReservationCreatedEvent received for reservation ID: " + event.getReservationId());

        reservationsCommandService.handleReservationsCreatedEvent(event);

        var getReservationsByUserIdQuery = new GetReservationsByUserIdQuery(event.getReservationId());

        var reservation = reservationsQueryService.handle(getReservationsByUserIdQuery);

        if(reservation.isEmpty()){
            System.out.println("Reservation with ID " + event.getReservationId() + " has been created.");
        } else {
            System.out.println("No reservation found with ID " + event.getReservationId());
        }
    }
}
