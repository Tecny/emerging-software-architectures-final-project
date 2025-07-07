package com.dtaquito_backend.dtaquito_backend.sportspaces.application.internal.queryservices;

import com.dtaquito_backend.dtaquito_backend.sportspaces.domain.model.aggregates.TrendSportData;
import com.dtaquito_backend.dtaquito_backend.sportspaces.domain.services.TrendSportDataQueryService;
import com.dtaquito_backend.dtaquito_backend.sportspaces.infrastructure.persistance.jpa.TrendSportDataRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TrendSportDataQueryServiceImpl implements TrendSportDataQueryService {

    private final TrendSportDataRepository repository;

    @Override
    public List<TrendSportData> findBySportSpaceId(Long sportSpaceId) {
        return repository.findBySportSpaceId(sportSpaceId);
    }
}
