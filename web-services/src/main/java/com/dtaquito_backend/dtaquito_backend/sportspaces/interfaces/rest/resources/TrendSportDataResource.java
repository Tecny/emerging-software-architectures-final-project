package com.dtaquito_backend.dtaquito_backend.sportspaces.interfaces.rest.resources;

public record TrendSportDataResource(
        Long id,
        Long sportSpaceId,
        String openingHour,
        String amountPeople,
        String currentMonth,
        String currentYear
) {}

