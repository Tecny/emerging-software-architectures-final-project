package com.dtaquito_backend.dtaquito_backend.sportspaces.domain.services;

import com.dtaquito_backend.dtaquito_backend.sportspaces.domain.model.aggregates.TrendSportData;

import java.util.List;

public interface TrendSportDataQueryService {
    List<TrendSportData> findBySportSpaceId(Long sportSpaceId);
}
