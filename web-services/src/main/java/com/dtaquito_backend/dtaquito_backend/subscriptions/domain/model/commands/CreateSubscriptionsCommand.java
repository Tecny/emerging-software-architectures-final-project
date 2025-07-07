package com.dtaquito_backend.dtaquito_backend.subscriptions.domain.model.commands;

public record CreateSubscriptionsCommand(Long planId, Long userId, String token) { }
