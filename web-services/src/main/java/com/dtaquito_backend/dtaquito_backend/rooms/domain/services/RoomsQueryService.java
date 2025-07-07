package com.dtaquito_backend.dtaquito_backend.rooms.domain.services;

import com.dtaquito_backend.dtaquito_backend.rooms.domain.model.aggregates.Rooms;
import com.dtaquito_backend.dtaquito_backend.rooms.domain.model.queries.GetRoomByUserIdQuery;

import java.util.List;

public interface RoomsQueryService {

    List<Rooms> getAllRooms();
    List<Rooms> handle(GetRoomByUserIdQuery query);
    List<Rooms> handleFindRoomsUserJoined(Long userId);
    List<Rooms> handleFindRoomsBySportSpacesOwner(Long userId);
}
