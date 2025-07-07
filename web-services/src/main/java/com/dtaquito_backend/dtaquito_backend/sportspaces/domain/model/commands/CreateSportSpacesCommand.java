package com.dtaquito_backend.dtaquito_backend.sportspaces.domain.model.commands;

import org.springframework.web.multipart.MultipartFile;

public record CreateSportSpacesCommand(String name, Long sportId, MultipartFile image, Double price, String address, String description, String openTime, String closeTime, Long gamemodeId, Double latitude, Double longitude) {

    public CreateSportSpacesCommand {

        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Name is required");
        }
        if (sportId == null) {
            throw new IllegalArgumentException("Sport ID is required");
        }
        if (image == null || image.isEmpty()) {
            throw new IllegalArgumentException("Image is required");
        }
        if (price == null) {
            throw new IllegalArgumentException("Price is required");
        }
        if (address == null || address.isBlank()) {
            throw new IllegalArgumentException("Address is required");
        }
        if (description == null || description.isBlank()) {
            throw new IllegalArgumentException("Description is required");
        }
        if (openTime == null || openTime.isBlank()) {
            throw new IllegalArgumentException("Start time is required");
        }
        if (closeTime == null || closeTime.isBlank()) {
            throw new IllegalArgumentException("End time is required");
        }
        if (gamemodeId == null) {
            throw new IllegalArgumentException("Gamemode is required");
        }
        if (latitude == null) {
            throw new IllegalArgumentException("Latitude is required");
        }
        if (longitude == null) {
            throw new IllegalArgumentException("Longitude is required");
        }
    }
}