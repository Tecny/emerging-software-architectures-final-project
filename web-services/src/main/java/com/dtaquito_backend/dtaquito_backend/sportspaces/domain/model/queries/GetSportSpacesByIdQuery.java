package com.dtaquito_backend.dtaquito_backend.sportspaces.domain.model.queries;

public record GetSportSpacesByIdQuery(Long id) {

    public GetSportSpacesByIdQuery {
        if (id == null) {
            throw new IllegalArgumentException("Id is required");
        }
    }
}
