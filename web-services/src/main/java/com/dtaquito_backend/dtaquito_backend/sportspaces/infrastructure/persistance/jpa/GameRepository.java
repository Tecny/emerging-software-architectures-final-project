package com.dtaquito_backend.dtaquito_backend.sportspaces.infrastructure.persistance.jpa;

import com.dtaquito_backend.dtaquito_backend.sportspaces.domain.model.entities.Game;
import com.dtaquito_backend.dtaquito_backend.sportspaces.domain.model.valueObjects.GameMode;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GameRepository extends JpaRepository<Game, Long> {

    boolean existsByGameMode(GameMode gameType);
}
