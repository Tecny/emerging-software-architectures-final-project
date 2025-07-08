package com.dtaquito_backend.dtaquito_backend.sportspaces.infrastructure.persistance.jpa;

import com.dtaquito_backend.dtaquito_backend.sportspaces.domain.model.aggregates.SportSpaces;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SportSpacesRepository extends JpaRepository<SportSpaces, Long> {

    List<SportSpaces> findByUserId(Long userId);

    @Query("SELECT CASE WHEN COUNT(r) > 0 OR :endTime <= :startTime THEN false ELSE true END FROM Reservations r WHERE r.sportSpaces.id = :sportSpaceId AND ((r.startTime < :endTime AND r.endTime > :startTime)) AND EXISTS (SELECT 1 FROM SportSpaces s WHERE s.id = :sportSpaceId AND (:startTime >= s.openTime AND :endTime <= s.closeTime) AND (:endTime > :startTime OR :endTime = s.closeTime)) AND r.gameDay = :gameDay")
    boolean isSportSpaceAvailable(@Param("sportSpaceId") Long sportSpaceId, @Param("gameDay") String gameDay, @Param("startTime") String startTime, @Param("endTime") String endTime);

}
