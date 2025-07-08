package com.dtaquito_backend.dtaquito_backend.sportspaces.interfaces.rest.resources;

import com.dtaquito_backend.dtaquito_backend.users.domain.model.aggregates.User;

public record SportSpacesResource(Long id, String name, Long sportId, String sportType, byte[] image, Double price, String address, String description, User user, String openTime, String closeTime,
                                  Long gamemodeId, String gamemodeType, Integer amount, double latitude, double longitude) {}