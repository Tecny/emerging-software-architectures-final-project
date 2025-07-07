package com.dtaquito_backend.dtaquito_backend.sportspaces.interfaces.rest.resources;

public record CreateTrendSportDataResource(
        Long sportSpaceId,
        String openingHour,
        String amountPeople,
        String currentMonth,
        String currentYear
) {
    public CreateTrendSportDataResource {
        if (sportSpaceId == null) {
            throw new IllegalArgumentException("Sport space ID is required");
        }
        if (openingHour == null || openingHour.isBlank()) {
            throw new IllegalArgumentException("Day hour is required");
        }
        if (amountPeople == null || amountPeople.isBlank()) {
            throw new IllegalArgumentException("Number of people is required");
        }
        if (currentMonth == null || currentMonth.isBlank()) {
            throw new IllegalArgumentException("Month is required");
        }
        if (currentYear == null || currentYear.isBlank()) {
            throw new IllegalArgumentException("Year is required");
        }
    }
}
