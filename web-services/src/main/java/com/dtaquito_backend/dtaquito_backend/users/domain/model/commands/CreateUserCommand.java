package com.dtaquito_backend.dtaquito_backend.users.domain.model.commands;

public record CreateUserCommand(String name, String email, String password, Long roleId) { }