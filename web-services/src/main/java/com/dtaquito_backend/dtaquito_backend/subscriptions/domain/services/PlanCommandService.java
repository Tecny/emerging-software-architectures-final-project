package com.dtaquito_backend.dtaquito_backend.subscriptions.domain.services;

import com.dtaquito_backend.dtaquito_backend.subscriptions.domain.model.commands.SeedPlanTypesCommand;

public interface PlanCommandService {

    void handle(SeedPlanTypesCommand command);
}
