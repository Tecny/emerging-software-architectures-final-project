package com.dtaquito_backend.dtaquito_backend.player_list.infrastructure.persistance.jpa;

import com.dtaquito_backend.dtaquito_backend.player_list.domain.model.aggregates.PlayerList;
import com.dtaquito_backend.dtaquito_backend.rooms.domain.model.aggregates.Rooms;
import com.dtaquito_backend.dtaquito_backend.users.domain.model.aggregates.User;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PlayerListRepository extends JpaRepository<PlayerList, Long> {

    boolean existsByRoomAndUser(Rooms room, User user);
    boolean existsByRoom_IdAndUser_Id(Long roomId, Long userId);

    @Transactional
    @Modifying
    @Query("DELETE FROM PlayerList pl WHERE pl.room.id = :roomId")
    void deleteByRoomId(@Param("roomId") Long roomId);

    @Query("SELECT pl FROM PlayerList pl WHERE pl.room.id = :roomId")
    List<PlayerList> findByRoomId(@Param("roomId") Long roomId);

    @Query("SELECT p FROM PlayerList p WHERE p.room.id = :roomId AND p.user.id = :userId")
    Optional<PlayerList> findByRoomAndUser(@Param("roomId") Long roomId,
                                           @Param("userId") Long userId);
}