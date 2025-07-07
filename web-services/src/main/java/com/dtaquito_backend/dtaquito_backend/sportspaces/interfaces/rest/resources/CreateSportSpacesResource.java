package com.dtaquito_backend.dtaquito_backend.sportspaces.interfaces.rest.resources;

import org.springframework.web.multipart.MultipartFile;

public record CreateSportSpacesResource(String name, Long sportId, MultipartFile image, Double price, String description, String openTime, String closeTime,
                                        Long gamemodeId, Double latitude, Double longitude) {

    public CreateSportSpacesResource {
        if (name == null) {
            throw new IllegalArgumentException("Name cannot be null");
        }
        if (sportId == null) {
            throw new IllegalArgumentException("SportId cannot be null");
        }
        if (image == null) {
        throw new IllegalArgumentException("ImageUrl cannot be null");
    }
        if (price == null) {
            throw new IllegalArgumentException("Price cannot be null");
        }
        if (description == null) {
            throw new IllegalArgumentException("Description cannot be null");
        }
        if (openTime == null) {
            throw new IllegalArgumentException("OpenTime cannot be null");
        }
        if (closeTime == null) {
            throw new IllegalArgumentException("CloseTime cannot be null");
        }
        if (gamemodeId == null) {
            throw new IllegalArgumentException("GameMode cannot be null");
        }
        if (latitude == null) {
            throw new IllegalArgumentException("Latitude cannot be null");
        }
        if (longitude == null) {
            throw new IllegalArgumentException("Longitude cannot be null");
        }
    }
}
