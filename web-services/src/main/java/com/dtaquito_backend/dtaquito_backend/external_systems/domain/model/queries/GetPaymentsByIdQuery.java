package com.dtaquito_backend.dtaquito_backend.external_systems.domain.model.queries;

public record GetPaymentsByIdQuery(Long id) {

    public GetPaymentsByIdQuery {

        if (id == null) {
            throw new IllegalArgumentException("Id is required");
        }
    }
}