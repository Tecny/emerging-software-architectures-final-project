package com.dtaquito_backend.dtaquito_backend.sportspaces.domain.model.aggregates;

import com.dtaquito_backend.dtaquito_backend.shared.domain.model.aggregates.AuditableAbstractAggregateRoot;
import com.dtaquito_backend.dtaquito_backend.sportspaces.domain.model.entities.Game;
import com.dtaquito_backend.dtaquito_backend.sportspaces.domain.model.entities.Sport;
import com.dtaquito_backend.dtaquito_backend.sportspaces.domain.model.valueObjects.GameMode;
import com.dtaquito_backend.dtaquito_backend.users.domain.model.aggregates.User;
import com.dtaquito_backend.dtaquito_backend.sportspaces.domain.model.commands.CreateSportSpacesCommand;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.io.IOException;

@Entity
@Getter
@Setter
@EntityListeners(AuditingEntityListener.class)
public class SportSpaces extends AuditableAbstractAggregateRoot<SportSpaces> {

    @Column(nullable = false)
    private String name;

    @ManyToOne
    @JoinColumn(name = "sport_id", nullable = false)
    private Sport sport;

    @Column(nullable = false, columnDefinition = "LONGBLOB")
    private byte[] image;

    @Column(nullable = false)
    private Double price;

    @Column(nullable = false, length = 100)
    private String address;

    @Column(nullable = false)
    private String description;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false, foreignKey = @ForeignKey(name = "FK_user_Id"))
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private User user;

    @Column(nullable = false)
    private String openTime;

    @Column(nullable = false)
    private String closeTime;

    @ManyToOne
    @JoinColumn(name = "game_id", nullable = false)
    private Game game;

    @Column(nullable = false)
    private Integer amount;

    @Column(nullable = false)
    private Double latitude;

    @Column(nullable = false)
    private Double longitude;

    public SportSpaces() {}

    public SportSpaces(CreateSportSpacesCommand command, User user, Sport sport, Game game) throws IOException {
        this.name = command.name();
        this.sport = sport;
        this.image = command.image().getBytes();
        this.price = command.price();
        this.address = command.address();
        this.description = command.description();
        this.user = user;
        this.openTime = command.openTime();
        this.closeTime = command.closeTime();
        this.game = game;
        this.amount = null;
        this.latitude    = command.latitude();
        this.longitude   = command.longitude();
        validateGameMode();
    }

    public void update(CreateSportSpacesCommand command, Sport sport, Game game) throws IOException {
        this.name = command.name();
        this.sport = sport;
        this.image = command.image().getBytes();
        this.address = command.address();
        this.description = command.description();
        this.openTime = command.openTime();
        this.closeTime = command.closeTime();
        this.game = game;
        validateGameMode();
    }

    private void validateGameMode() {
        if (this.amount == null) {
            double halfPrice = this.price / 2.0;
            int maxPlayers = this.game.getGameMode().getMaxPlayers();
            double divided = halfPrice / maxPlayers;

            this.amount = (divided - Math.floor(divided) >= 0.5)
                    ? (int) Math.ceil(divided)
                    : (int) Math.floor(divided);
        }

        int maxPlayers = this.game.getGameMode().getMaxPlayers();
        double halfPrice = this.price / 2.0;
        double divided = halfPrice / maxPlayers;

        int calculatedAmount = (divided - Math.floor(divided) >= 0.5)
                ? (int) Math.ceil(divided)
                : (int) Math.floor(divided);

        if (this.amount > calculatedAmount) {
            throw new IllegalArgumentException("The advance amount exceeds the calculated amount based on the original price and game mode.");
        }

        if ((this.game.getGameMode() == GameMode.FUTBOL_11 || this.game.getGameMode() == GameMode.FUTBOL_7 || this.game.getGameMode() == GameMode.FUTBOL_8 || this.game.getGameMode() == GameMode.FUTBOL_5) && this.sport.getId() != 1) {
            throw new IllegalArgumentException("The sport ID must be 1 for the game modes FUTBOL_11, FUTBOL_7, FUTBOL_8, and FUTBOL_5");
        }

        if (this.game.getGameMode() == GameMode.BILLAR_3 && this.sport.getId() != 2) {
            throw new IllegalArgumentException("The sport ID must be 2 for the game mode BILLAR_3");
        }
    }
}