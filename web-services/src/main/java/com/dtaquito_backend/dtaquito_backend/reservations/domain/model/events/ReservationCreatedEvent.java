package com.dtaquito_backend.dtaquito_backend.reservations.domain.model.events;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class ReservationCreatedEvent extends ApplicationEvent {

    private final Long reservationId;

    public ReservationCreatedEvent(Object source, Long reservationId) {
        super(source);
        this.reservationId = reservationId;
    }
}