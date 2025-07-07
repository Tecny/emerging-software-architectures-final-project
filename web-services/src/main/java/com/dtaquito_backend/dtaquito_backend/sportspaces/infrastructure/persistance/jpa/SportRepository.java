package com.dtaquito_backend.dtaquito_backend.sportspaces.infrastructure.persistance.jpa;

import com.dtaquito_backend.dtaquito_backend.sportspaces.domain.model.entities.Sport;
import com.dtaquito_backend.dtaquito_backend.sportspaces.domain.model.valueObjects.SportTypes;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SportRepository extends JpaRepository<Sport, Long> {
    boolean existsBySportType(SportTypes sportType);
}