package com.dtaquito_backend.dtaquito_backend.sportspaces.application.internal.commandservices;

import com.dtaquito_backend.dtaquito_backend.sportspaces.domain.model.commands.SeedGameTypeCommand;
import com.dtaquito_backend.dtaquito_backend.sportspaces.domain.model.entities.Game;
import com.dtaquito_backend.dtaquito_backend.sportspaces.domain.model.valueObjects.GameMode;
import com.dtaquito_backend.dtaquito_backend.sportspaces.domain.services.GameCommandService;
import com.dtaquito_backend.dtaquito_backend.sportspaces.infrastructure.persistance.jpa.GameRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

import java.util.Arrays;

@Service
public class GameCommandServiceImpl implements GameCommandService {

    private final GameRepository gameRepository;

    public GameCommandServiceImpl(GameRepository gameRepository) {
        this.gameRepository = gameRepository;
    }

    @PostConstruct
    public void init() { handle(new SeedGameTypeCommand()); }

    @Override
    public void handle(SeedGameTypeCommand command) {
        Arrays.stream(GameMode.values()).forEach(gameType -> {
            if (!gameRepository.existsByGameMode(gameType)) {
                gameRepository.save(new Game(gameType));
            }
        });
    }
}
