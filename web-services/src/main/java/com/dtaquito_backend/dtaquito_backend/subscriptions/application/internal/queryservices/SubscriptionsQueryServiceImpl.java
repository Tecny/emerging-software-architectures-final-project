package com.dtaquito_backend.dtaquito_backend.subscriptions.application.internal.queryservices;

import com.dtaquito_backend.dtaquito_backend.subscriptions.domain.model.aggregates.Subscription;
import com.dtaquito_backend.dtaquito_backend.subscriptions.domain.model.queries.GetSubscriptionsByIdQuery;
import com.dtaquito_backend.dtaquito_backend.subscriptions.domain.services.SubscriptionsQueryService;
import com.dtaquito_backend.dtaquito_backend.subscriptions.infrastructure.persistance.jpa.SubscriptionsRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class SubscriptionsQueryServiceImpl implements SubscriptionsQueryService {

    private final SubscriptionsRepository subscriptionsRepository;

    public SubscriptionsQueryServiceImpl(SubscriptionsRepository subscriptionsRepository){
        this.subscriptionsRepository = subscriptionsRepository;
    }

    @Override
    public Optional<Subscription> handle(GetSubscriptionsByIdQuery query) {
        return subscriptionsRepository.findById(query.id());
    }

    @Override
    public Optional<Subscription> getSubscriptionByUserId(Long userId) {
        List<Subscription> subscriptions = subscriptionsRepository.findByUserId(userId);
        if (subscriptions.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(subscriptions.get(0));
    }
}
