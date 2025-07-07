package com.dtaquito_backend.dtaquito_backend.sportspaces.infrastructure.persistance.jpa;

import com.dtaquito_backend.dtaquito_backend.sportspaces.domain.model.aggregates.SportSpaces;
import com.dtaquito_backend.dtaquito_backend.sportspaces.domain.model.aggregates.TrendSportData;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalTime;
import java.util.List;

public interface TrendSportDataRepository extends JpaRepository<TrendSportData, Long> {
    List<TrendSportData> findBySportSpaceId(Long sportSpaceId);
    boolean existsBySportSpaceIdAndCurrentMonthAndCurrentYear(Long sportSpaceId, String currentMonth, String currentYear);
    void deleteAllBySportSpace(SportSpaces sportSpace);
    List<TrendSportData> findBySportSpaceIdAndCurrentMonthAndCurrentYear(Long sportSpaceId, String currentMonth, String currentYear);
    List<TrendSportData> findBySportSpaceIdAndCurrentMonthAndCurrentYearAndOpeningHour(
            Long sportSpaceId,
            String currentMonth,
            String currentYear,
            String openingHour
    );
}
