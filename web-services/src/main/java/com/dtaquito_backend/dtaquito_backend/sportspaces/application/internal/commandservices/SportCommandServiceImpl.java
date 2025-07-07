package com.dtaquito_backend.dtaquito_backend.sportspaces.application.internal.commandservices;

import com.dtaquito_backend.dtaquito_backend.sportspaces.domain.model.commands.SeedSportTypeCommand;
import com.dtaquito_backend.dtaquito_backend.sportspaces.domain.model.entities.Sport;
import com.dtaquito_backend.dtaquito_backend.sportspaces.domain.model.valueObjects.SportTypes;
import com.dtaquito_backend.dtaquito_backend.sportspaces.domain.services.SportCommandService;
import com.dtaquito_backend.dtaquito_backend.sportspaces.infrastructure.persistance.jpa.SportRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

import java.util.Arrays;

@Service
public class SportCommandServiceImpl implements SportCommandService {

    private final SportRepository sportRepository;

    public SportCommandServiceImpl(SportRepository sportRepository) {
        this.sportRepository = sportRepository;
    }

    @PostConstruct
    public void init() {
        handle(new SeedSportTypeCommand());
    }

    @Override
    public void handle(SeedSportTypeCommand command) {
        Arrays.stream(SportTypes.values()).forEach(sportType -> {
            if (!sportRepository.existsBySportType(sportType)) {
                sportRepository.save(new Sport(sportType));
            }
        });
    }
}