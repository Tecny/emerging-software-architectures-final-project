package com.dtaquito_backend.dtaquito_backend.sportspaces.domain.services;

import com.dtaquito_backend.dtaquito_backend.sportspaces.domain.model.aggregates.SportSpaces;
import com.dtaquito_backend.dtaquito_backend.sportspaces.domain.model.queries.GetSportSpacesByIdQuery;
import com.dtaquito_backend.dtaquito_backend.sportspaces.domain.model.queries.GetSportSpacesByUserIdQuery;

import java.util.List;
import java.util.Optional;

public interface SportSpacesQueryService {

    Optional<SportSpaces> handle(GetSportSpacesByIdQuery query);
    List<SportSpaces> getAllSportSpaces();

    List<SportSpaces> handle(GetSportSpacesByUserIdQuery query);
    boolean isSportSpaceAvailable(Long sportSpaceId, String date, String openTime, String closeTime);
}
