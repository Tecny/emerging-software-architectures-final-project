package com.dtaquito_backend.dtaquito_backend.reservations.domain.model.aggregates;

import com.dtaquito_backend.dtaquito_backend.reservations.domain.model.commands.CreateReservationsCommand;
import com.dtaquito_backend.dtaquito_backend.reservations.domain.model.valueobjects.Status;
import com.dtaquito_backend.dtaquito_backend.reservations.domain.model.valueobjects.Type;
import com.dtaquito_backend.dtaquito_backend.shared.domain.model.aggregates.AuditableAbstractAggregateRoot;
import com.dtaquito_backend.dtaquito_backend.sportspaces.domain.model.aggregates.SportSpaces;
import com.dtaquito_backend.dtaquito_backend.users.domain.model.aggregates.User;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.Getter;

import lombok.Setter;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Getter
@Setter
@EntityListeners(AuditingEntityListener.class)
public class Reservations extends AuditableAbstractAggregateRoot<Reservations> {

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String gameDay;

    @Column(nullable = false)
    private String startTime;

    @Column(nullable = false)
    private String endTime;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false, foreignKey = @ForeignKey(name = "FK_user_id3"))
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private User user;

    @ManyToOne
    @JoinColumn(name = "sport_spaces_id", nullable = false, foreignKey = @ForeignKey(name = "FK_space_id"))
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private SportSpaces sportSpaces;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Status status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Type type;

    @Column(nullable = false)
    private String reservationName;

    protected Reservations() {}

    public Reservations(CreateReservationsCommand command, User user, SportSpaces sportSpaces) {
        this.gameDay = command.gameDay();
        this.startTime = command.startTime();
        this.endTime = command.endTime();
        this.user = user;
        this.sportSpaces = sportSpaces;
        this.type = Type.valueOf(command.type());
        this.reservationName = command.reservationName();
    }
}
