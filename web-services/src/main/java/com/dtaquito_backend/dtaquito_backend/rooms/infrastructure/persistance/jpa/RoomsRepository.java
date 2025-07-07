package com.dtaquito_backend.dtaquito_backend.rooms.infrastructure.persistance.jpa;

import com.dtaquito_backend.dtaquito_backend.rooms.domain.model.aggregates.Rooms;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;


public interface RoomsRepository extends JpaRepository<Rooms, Long> {

    @Query("SELECT r FROM Rooms r WHERE r.reservations.id = :reservationId")
    List<Rooms> findByReservationsId(Long reservationId);

    @Query("SELECT r FROM Rooms r WHERE r.reservations.user.id = :userId")
    List<Rooms> findRoomsByReservationUserId(@Param("userId") Long userId);

    @Query("SELECT pl.room FROM PlayerList pl WHERE pl.user.id = :userId")
    List<Rooms> findRoomsByUserId(@Param("userId") Long userId);

    @Query("SELECT r FROM Rooms r WHERE r.reservations.sportSpaces.user.id = :userId")
    List<Rooms> findRoomsBySportSpaceOwnerId(@Param("userId") Long userId);
}