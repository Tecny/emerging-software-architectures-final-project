package com.dtaquito_backend.dtaquito_backend.rooms.interfaces.rest.transform;

import com.dtaquito_backend.dtaquito_backend.rooms.domain.model.commands.CreateRoomCommand;
import com.dtaquito_backend.dtaquito_backend.rooms.interfaces.rest.resources.CreateRoomsResource;

public class CreateRoomsCommandFromResourceAssembler {

    public static CreateRoomCommand toCommandFromResource(CreateRoomsResource resource) {
        return new CreateRoomCommand(
                resource.reservationId(),
                resource.playerListId()
        );
    }
}
