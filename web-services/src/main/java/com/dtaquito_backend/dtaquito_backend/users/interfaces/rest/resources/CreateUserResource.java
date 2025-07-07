package com.dtaquito_backend.dtaquito_backend.users.interfaces.rest.resources;

public record CreateUserResource(
        String name,
        String email,
        String password,
        Long roleId) { }
