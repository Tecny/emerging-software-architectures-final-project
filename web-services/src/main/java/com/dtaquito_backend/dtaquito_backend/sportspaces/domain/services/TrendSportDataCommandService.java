package com.dtaquito_backend.dtaquito_backend.sportspaces.domain.services;

public interface TrendSportDataCommandService {

    void createInitialDataForSportSpace(Long sportSpaceId);
    void deleteAllDataForSportSpace(Long sportSpaceId);
}
