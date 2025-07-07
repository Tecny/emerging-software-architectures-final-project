package com.dtaquito_backend.dtaquito_backend.users.domain.model.queries;

public record GetUserByIdQuery(Long id) {

    public GetUserByIdQuery {

        if (id == null) {
            throw new IllegalArgumentException("Id is required");
        }
    }
}