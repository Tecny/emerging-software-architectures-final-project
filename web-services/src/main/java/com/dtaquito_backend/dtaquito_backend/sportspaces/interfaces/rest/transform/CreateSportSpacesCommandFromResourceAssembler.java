package com.dtaquito_backend.dtaquito_backend.sportspaces.interfaces.rest.transform;

import com.dtaquito_backend.dtaquito_backend.sportspaces.domain.model.commands.CreateSportSpacesCommand;
import com.dtaquito_backend.dtaquito_backend.sportspaces.interfaces.rest.resources.CreateSportSpacesResource;

public class CreateSportSpacesCommandFromResourceAssembler {

    public static CreateSportSpacesCommand toCommandFromResource(CreateSportSpacesResource resource, String address
                                                                 ) {
        return new CreateSportSpacesCommand(
                resource.name(),
                resource.sportId(),
                resource.image(),
                resource.price(),
                address,
                resource.description(),
                resource.openTime(),
                resource.closeTime(),
                resource.gamemodeId(),
                resource.latitude(),
                resource.longitude()
        );
    }
}