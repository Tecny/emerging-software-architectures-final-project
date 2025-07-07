package com.dtaquito_backend.dtaquito_backend.subscriptions.domain.services;

import java.util.Optional;

import com.dtaquito_backend.dtaquito_backend.subscriptions.domain.model.aggregates.Subscription;
import com.dtaquito_backend.dtaquito_backend.subscriptions.domain.model.commands.CreateSubscriptionsCommand;
import com.dtaquito_backend.dtaquito_backend.subscriptions.domain.model.events.SubscriptionCreatedEvent;

public interface SubscriptionsCommandService {

    Optional<Subscription> handle(CreateSubscriptionsCommand command);

    void handleSubscriptionCreatedEvent(SubscriptionCreatedEvent event);
}