package com.dtaquito_backend.dtaquito_backend.external_systems.domain.model.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class Address {
    @JsonProperty("road")
    private String road;

    @JsonProperty("house_number")
    private String houseNumber;

    @JsonProperty("name")
    private String name;

    @JsonProperty("suburb")
    private String suburb;

    @JsonProperty("country")
    private String country;
}