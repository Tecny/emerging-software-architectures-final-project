package com.dtaquito_backend.dtaquito_backend.external_systems.domain.model.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class LocationIQResponse {

    @JsonProperty("lat")
    private String lat;

    @JsonProperty("lon")
    private String lon;

    @JsonProperty("address")
    private Address address;

    public String getFormattedAddress() {
        List<String> parts = new ArrayList<>();
        if (address.getRoad() != null && !address.getRoad().isBlank()) {
            parts.add(address.getRoad());
        }
        if (address.getHouseNumber() != null && !address.getHouseNumber().isBlank()) {
            parts.add(address.getHouseNumber());
        }
        if (address.getName() != null && !address.getName().isBlank()) {
            parts.add(address.getName());
        }
        if (address.getSuburb() != null && !address.getSuburb().isBlank()) {
            parts.add(address.getSuburb());
        }
        if (address.getCountry() != null && !address.getCountry().isBlank()) {
            parts.add(address.getCountry());
        }
        return String.join(", ", parts);
    }
}
