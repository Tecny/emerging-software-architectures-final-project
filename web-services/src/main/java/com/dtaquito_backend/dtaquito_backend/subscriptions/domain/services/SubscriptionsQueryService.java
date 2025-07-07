package com.dtaquito_backend.dtaquito_backend.subscriptions.domain.services;

import com.dtaquito_backend.dtaquito_backend.subscriptions.domain.model.aggregates.Subscription;
import com.dtaquito_backend.dtaquito_backend.subscriptions.domain.model.queries.GetSubscriptionsByIdQuery;

import java.util.Optional;

public interface SubscriptionsQueryService {

    Optional<Subscription> handle(GetSubscriptionsByIdQuery query);
    Optional<Subscription> getSubscriptionByUserId(Long userId);
}
