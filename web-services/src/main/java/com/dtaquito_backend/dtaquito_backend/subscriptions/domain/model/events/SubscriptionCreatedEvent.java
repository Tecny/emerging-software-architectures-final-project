package com.dtaquito_backend.dtaquito_backend.subscriptions.domain.model.events;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public final class SubscriptionCreatedEvent extends ApplicationEvent {

    private final Long subscriptionId;

    public SubscriptionCreatedEvent(Object source, Long subscriptionId) {
        super(source);
        this.subscriptionId = subscriptionId;
    }
}
