package com.dtaquito_backend.dtaquito_backend.subscriptions.application.internal.eventhandlers;

import com.dtaquito_backend.dtaquito_backend.subscriptions.domain.model.events.SubscriptionCreatedEvent;
import com.dtaquito_backend.dtaquito_backend.subscriptions.domain.model.queries.GetSubscriptionsByIdQuery;
import com.dtaquito_backend.dtaquito_backend.subscriptions.domain.services.SubscriptionsCommandService;
import com.dtaquito_backend.dtaquito_backend.subscriptions.domain.services.SubscriptionsQueryService;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

@Service
public class SubscriptionCreatedEventHandler {

    private final SubscriptionsQueryService subscriptionQueryService;
    private final SubscriptionsCommandService subscriptionCommandService;

    public SubscriptionCreatedEventHandler(SubscriptionsQueryService subscriptionQueryService, SubscriptionsCommandService subscriptionCommandService) {
        this.subscriptionQueryService = subscriptionQueryService;
        this.subscriptionCommandService = subscriptionCommandService;
    }

    @EventListener(SubscriptionCreatedEvent.class)
    public void on(SubscriptionCreatedEvent event) {
        System.out.println("SuscriptionCreatedEvent received for subscription ID: " + event.getSubscriptionId());

        subscriptionCommandService.handleSubscriptionCreatedEvent(event);

        var getSubscriptionByIdQuery = new GetSubscriptionsByIdQuery(event.getSubscriptionId());

        var subscription = subscriptionQueryService.handle(getSubscriptionByIdQuery);

        if (subscription.isPresent()) {
            System.out.println("Suscription with ID " + event.getSubscriptionId() + " has been created.");
        } else {
            System.out.println("No subscription found with ID " + event.getSubscriptionId());
        }
    }
}
