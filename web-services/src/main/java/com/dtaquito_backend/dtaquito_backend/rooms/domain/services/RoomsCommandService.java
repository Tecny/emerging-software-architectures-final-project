package com.dtaquito_backend.dtaquito_backend.rooms.domain.services;

import com.dtaquito_backend.dtaquito_backend.rooms.domain.model.aggregates.Rooms;
import org.springframework.scheduling.annotation.Scheduled;


public interface RoomsCommandService {

    @Scheduled(fixedRate = 60000)
    void deleteRoomByEndTimeConcluded();

    void transferToCreator(Rooms room);
    void refundToUsers(Long playerListsId);

    void addPlayerToRoomAndChat(Long roomId, Long userId);

    boolean isRoomCreator(Long roomId, Long userId);

    void removePlayerFromRoomAndChat(Long roomId, Long userId);
}