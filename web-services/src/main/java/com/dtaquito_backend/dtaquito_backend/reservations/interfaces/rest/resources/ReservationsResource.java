package com.dtaquito_backend.dtaquito_backend.reservations.interfaces.rest.resources;

import com.dtaquito_backend.dtaquito_backend.reservations.domain.model.valueobjects.Status;
import com.dtaquito_backend.dtaquito_backend.reservations.domain.model.valueobjects.Type;


public record ReservationsResource(Long id, String gameDay, String startTime, String endTime, Long userId, Long sportSpacesId, Status status, Type type, String reservationName) {

    public ReservationsResource {
        if (id == null) {
            throw new IllegalArgumentException("Id cannot be null");
        }
        if (gameDay == null) {
            throw new IllegalArgumentException("Date cannot be null");
        }
        if (startTime == null) {
            throw new IllegalArgumentException("StartTime cannot be null");
        }
        if (endTime == null) {
            throw new IllegalArgumentException("EndTime cannot be null");
        }
        if (userId == null) {
            throw new IllegalArgumentException("UserId cannot be null");
        }
        if (sportSpacesId == null) {
            throw new IllegalArgumentException("SportSpacesId cannot be null");
        }
        if (status == null) {
            throw new IllegalArgumentException("Status cannot be null");
        }
        if (type == null) {
            throw new IllegalArgumentException("Type cannot be null");
        }
        if (reservationName == null) {
            throw new IllegalArgumentException("ReservationName cannot be null");
        }
    }
}
