package com.dtaquito_backend.dtaquito_backend.reservations.interfaces.rest.resources;

public record CreateReservationsResource(String gameDay, String startTime, String endTime, Long sportSpacesId, String type, String reservationName) {

    public CreateReservationsResource {
        if (gameDay == null) {
            throw new IllegalArgumentException("Date cannot be null");
        }
        if (startTime == null) {
            throw new IllegalArgumentException("StartTime cannot be null");
        }
        if (endTime == null) {
            throw new IllegalArgumentException("EndTime cannot be null");
        }
        if (sportSpacesId == null) {
            throw new IllegalArgumentException("SportSpacesId cannot be null");
        }
        if (type == null) {
            throw new IllegalArgumentException("Type cannot be null");
        }
        if (reservationName == null) {
            throw new IllegalArgumentException("ReservationName cannot be null");
        }
    }
}
