package com.dtaquito_backend.dtaquito_backend.sportspaces.application.internal.commandservices;
import com.dtaquito_backend.dtaquito_backend.sportspaces.domain.model.aggregates.SportSpaces;
import com.dtaquito_backend.dtaquito_backend.sportspaces.domain.model.commands.CreateTrendSportDataCommand;
import com.dtaquito_backend.dtaquito_backend.sportspaces.domain.services.TrendSportDataCommandService;
import com.dtaquito_backend.dtaquito_backend.sportspaces.infrastructure.persistance.jpa.TrendSportDataRepository;
import com.dtaquito_backend.dtaquito_backend.sportspaces.infrastructure.persistance.jpa.SportSpacesRepository;
import com.dtaquito_backend.dtaquito_backend.sportspaces.domain.model.aggregates.TrendSportData;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TrendSportDataCommandServiceImpl implements TrendSportDataCommandService {

    private final TrendSportDataRepository repository;
    private final SportSpacesRepository sportSpacesRepository;

    @Override
    public void createInitialDataForSportSpace(Long sportSpaceId) {
        ZonedDateTime now = ZonedDateTime.now(ZoneId.of("America/Lima"));
        String month = now.getMonth().getDisplayName(java.time.format.TextStyle.FULL, new java.util.Locale("es"));
        String year = String.valueOf(now.getYear());

        SportSpaces sportSpace = sportSpacesRepository.findById(sportSpaceId)
                .orElseThrow(() -> new IllegalArgumentException("Espacio deportivo no encontrado con ID: " + sportSpaceId));

        // Obtener las horas directamente desde el sportSpace
        LocalTime start = LocalTime.parse(sportSpace.getOpenTime());
        LocalTime end = LocalTime.parse(sportSpace.getCloseTime()).minusHours(1);

        if (start.isAfter(end)) {
            throw new IllegalArgumentException("La hora de apertura no puede ser posterior a la hora de cierre.");
        }

        List<TrendSportData> dataList = new ArrayList<>();

        long hoursBetween = java.time.Duration.between(start, end).toHours();

        for (int i = 0; i <= hoursBetween; i++) {
            String hourFormatted = start.toString();
            CreateTrendSportDataCommand command = new CreateTrendSportDataCommand(
                    sportSpaceId, hourFormatted, "0", month, year
            );
            TrendSportData data = new TrendSportData(command);
            data.setSportSpace(sportSpace);
            dataList.add(data);
            start = start.plusHours(1);
        }
        repository.saveAll(dataList);
    }

    //@Scheduled(cron = "0 0 0 * * ?") // Se ejecuta a las 00:00 hrs todos los días
    @Scheduled(cron = "0 * * * * ?")
    public void checkAndCreateDataForNewMonthAndYear() {
        ZonedDateTime now = ZonedDateTime.now(ZoneId.of("America/Lima"));
        String currentMonth = now.getMonth().getDisplayName(java.time.format.TextStyle.FULL, new java.util.Locale("es"));
        String currentYear = String.valueOf(now.getYear());

        List<SportSpaces> sportSpacesList = sportSpacesRepository.findAll();
        for (SportSpaces sportSpace : sportSpacesList) {
            // Verificar si ya existe un registro para el mes y año actuales
            boolean exists = repository.existsBySportSpaceIdAndCurrentMonthAndCurrentYear(
                    sportSpace.getId(), currentMonth, currentYear);

            if (!exists) {
                createInitialDataForSportSpace(sportSpace.getId());
            }
        }
    }

    @Override
    public void deleteAllDataForSportSpace(Long sportSpaceId) {
        SportSpaces sportSpace = sportSpacesRepository.findById(sportSpaceId)
                .orElseThrow(() -> new IllegalArgumentException("Espacio deportivo no encontrado con ID: " + sportSpaceId));

        repository.deleteAllBySportSpace(sportSpace);
    }

}