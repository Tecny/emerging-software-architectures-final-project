package com.dtaquito_backend.dtaquito_backend.deposit.domain.model.queries;

public record GetDepositByIdQuery(Long id) {

    public GetDepositByIdQuery {
        if (id == null) {
            throw new IllegalArgumentException("Id is required");
        }
    }

    public Long getDepositId() {
        return id;
    }
}