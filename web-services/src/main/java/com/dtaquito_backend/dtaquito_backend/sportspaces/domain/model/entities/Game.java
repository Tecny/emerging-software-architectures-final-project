package com.dtaquito_backend.dtaquito_backend.sportspaces.domain.model.entities;

import com.dtaquito_backend.dtaquito_backend.sportspaces.domain.model.valueObjects.GameMode;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "game_types")
public class Game {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private GameMode gameMode;

    public Game() {}

    public Game(GameMode gameMode) { this.gameMode = gameMode; }
}
