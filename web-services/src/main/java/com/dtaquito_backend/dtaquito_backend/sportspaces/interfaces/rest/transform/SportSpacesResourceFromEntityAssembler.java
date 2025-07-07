package com.dtaquito_backend.dtaquito_backend.sportspaces.interfaces.rest.transform;

import com.dtaquito_backend.dtaquito_backend.sportspaces.domain.model.aggregates.SportSpaces;
import com.dtaquito_backend.dtaquito_backend.sportspaces.interfaces.rest.resources.SportSpacesResource;

public class SportSpacesResourceFromEntityAssembler {

    public static SportSpacesResource toResourceFromEntity(SportSpaces entity) {
        return new SportSpacesResource(entity.getId(), entity.getName(), entity.getSport().getId(), entity.getSport().getSportType().name().toUpperCase(), entity.getImage(), entity.getPrice(), entity.getAddress(), entity.getDescription(), entity.getUser(), entity.getOpenTime(), entity.getCloseTime(), entity.getGame().getId(), entity.getGame().getGameMode().name(), entity.getAmount(), entity.getLatitude(), entity.getLongitude());
    }
}