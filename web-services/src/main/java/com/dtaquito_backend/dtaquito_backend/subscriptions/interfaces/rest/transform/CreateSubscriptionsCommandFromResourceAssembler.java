package com.dtaquito_backend.dtaquito_backend.subscriptions.interfaces.rest.transform;

import com.dtaquito_backend.dtaquito_backend.subscriptions.domain.model.commands.CreateSubscriptionsCommand;
import com.dtaquito_backend.dtaquito_backend.subscriptions.interfaces.rest.resources.CreateSubscriptionsResource;

public class CreateSubscriptionsCommandFromResourceAssembler {

    public static CreateSubscriptionsCommand toCommandFromResource(CreateSubscriptionsResource resource) {
        return new CreateSubscriptionsCommand(resource.planId(), resource.userId(), resource.token());
    }
}