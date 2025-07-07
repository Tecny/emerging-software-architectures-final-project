package com.dtaquito_backend.dtaquito_backend.reservations.domain.model.commands;

public record CreateReservationsCommand(String gameDay, String startTime, String endTime, Long sportSpacesId, String type, String reservationName) {

    public CreateReservationsCommand {
        if (gameDay == null) {
            throw new IllegalArgumentException("GameDay is required");
        }
        if (startTime == null) {
            throw new IllegalArgumentException("StartTime is required");
        }
        if (endTime == null) {
            throw new IllegalArgumentException("EndTime is required");
        }
        if (sportSpacesId == null) {
            throw new IllegalArgumentException("Sport spaces ID is required");
        }
        if (type == null) {
            throw new IllegalArgumentException("Type is required");
        }
        if (reservationName == null) {
            throw new IllegalArgumentException("Reservation name is required");
        }
    }
}
