package com.dtaquito_backend.dtaquito_backend.users.interfaces.rest.resources;

import java.math.BigDecimal;

public record UserResource(Long id, String name, String email, String roleType, BigDecimal credits) { }