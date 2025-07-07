package com.dtaquito_backend.dtaquito_backend.reservations.infrastructure.persistance.jpa;

import com.dtaquito_backend.dtaquito_backend.reservations.domain.model.aggregates.Reservations;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReservationsRepository extends JpaRepository<Reservations, Long> {

    List<Reservations> findByUserId(Long userId);
    List<Reservations> findBySportSpacesIdAndGameDay(Long sportSpacesId, String gameDay);
}
