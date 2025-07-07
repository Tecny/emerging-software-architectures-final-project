package com.dtaquito_backend.dtaquito_backend.subscriptions.interfaces.rest.transform;

import com.dtaquito_backend.dtaquito_backend.subscriptions.domain.model.aggregates.Subscription;
import com.dtaquito_backend.dtaquito_backend.subscriptions.interfaces.rest.resources.SubscriptionsResource;

public class SubscriptionsResourceFromEntityAssembler {

    public static SubscriptionsResource toResourceFromEntity(Subscription entity) {
        return new SubscriptionsResource(entity.getId(), entity.getPlan().getId(), entity.getUser(), entity.getPlan().getPlanType().name().toUpperCase(), (long) entity.getAllowedSportSpaces());
    }
}
