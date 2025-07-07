package com.dtaquito_backend.dtaquito_backend.rooms.interfaces.rest.resources;
import com.dtaquito_backend.dtaquito_backend.sportspaces.domain.model.valueObjects.GameMode;
import com.dtaquito_backend.dtaquito_backend.sportspaces.domain.model.valueObjects.SportTypes;

public record SportSpaceDTO(
        Long id,
        String name,
        byte[] image,
        String address,
        SportTypes sportType,
        GameMode gamemode,
        Double price,
        Integer amount
) {
}
