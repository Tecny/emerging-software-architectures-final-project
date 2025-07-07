package com.dtaquito_backend.dtaquito_backend.sportspaces.interfaces.rest.transform;

import com.dtaquito_backend.dtaquito_backend.sportspaces.domain.model.commands.CreateTrendSportDataCommand;
import com.dtaquito_backend.dtaquito_backend.sportspaces.interfaces.rest.resources.CreateTrendSportDataResource;

public class CreateTrendSportDataCommandFromResourceAssembler {
    public static CreateTrendSportDataCommand toCommandFromResource(CreateTrendSportDataResource resource) {
        return new CreateTrendSportDataCommand(
                resource.sportSpaceId(),
                resource.openingHour(),
                resource.amountPeople(),
                resource.currentMonth(),
                resource.currentYear()
        );
    }
}