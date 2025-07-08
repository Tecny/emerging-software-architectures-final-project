package com.dtaquito_backend.dtaquito_backend.subscriptions.interfaces.rest.resources;

import com.dtaquito_backend.dtaquito_backend.users.domain.model.aggregates.User;

public record SubscriptionsResource(Long id, Long planId, User user, String planType, Long allowedSportSpaces) {
}
