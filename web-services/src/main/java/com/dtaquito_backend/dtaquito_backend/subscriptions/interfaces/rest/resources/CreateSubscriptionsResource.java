package com.dtaquito_backend.dtaquito_backend.subscriptions.interfaces.rest.resources;

public record CreateSubscriptionsResource(Long planId, Long userId, String token, Long allowedSportSpaces) {}
