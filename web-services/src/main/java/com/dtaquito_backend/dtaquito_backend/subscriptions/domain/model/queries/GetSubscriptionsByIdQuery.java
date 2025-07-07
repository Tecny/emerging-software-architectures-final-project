package com.dtaquito_backend.dtaquito_backend.subscriptions.domain.model.queries;

public record GetSubscriptionsByIdQuery(Long id) {

    public GetSubscriptionsByIdQuery {
        if (id == null) {
            throw new IllegalArgumentException("Id is required");
        }
    }
}
