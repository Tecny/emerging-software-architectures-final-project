package com.dtaquito_backend.dtaquito_backend.sportspaces.interfaces.rest.transform;

import com.dtaquito_backend.dtaquito_backend.sportspaces.domain.model.aggregates.TrendSportData;
import com.dtaquito_backend.dtaquito_backend.sportspaces.interfaces.rest.resources.TrendSportDataResource;

public class TrendSportDataResourceFromEntityAssembler {
    public static TrendSportDataResource toResourceFromEntity(TrendSportData entity) {
        return new TrendSportDataResource(
                entity.getId(),
                entity.getSportSpace().getId(),
                entity.getOpeningHour(),
                entity.getAmountPeople(),
                entity.getCurrentMonth(),
                entity.getCurrentYear()
        );
    }
}