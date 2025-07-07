package com.dtaquito_backend.dtaquito_backend.sportspaces.domain.model.entities;

import com.dtaquito_backend.dtaquito_backend.sportspaces.domain.model.valueObjects.SportTypes;
import jakarta.persistence.*;
import lombok.Getter;

@Entity
@Getter
@Table(name = "sport_types")
public class Sport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private SportTypes sportType;

    public Sport() {}

    public Sport(SportTypes sportType) {
        this.sportType = sportType;
    }
}
