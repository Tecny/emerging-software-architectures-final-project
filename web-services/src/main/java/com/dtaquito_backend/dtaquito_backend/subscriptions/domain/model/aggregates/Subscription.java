package com.dtaquito_backend.dtaquito_backend.subscriptions.domain.model.aggregates;

import com.dtaquito_backend.dtaquito_backend.shared.domain.model.aggregates.AuditableAbstractAggregateRoot;
import com.dtaquito_backend.dtaquito_backend.subscriptions.domain.model.entities.Plan;
import com.dtaquito_backend.dtaquito_backend.users.domain.model.aggregates.User;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
public class Subscription extends AuditableAbstractAggregateRoot<Subscription> {

    @ManyToOne
    @JoinColumn(name = "plan_id", nullable = false)
    @Setter
    private Plan plan;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false, foreignKey = @ForeignKey(name = "FK_user_Id_unique"))
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private User user;

    @Column(nullable = false)
    private int allowedSportSpaces = 0;

    public Subscription() {}

    public Subscription(Plan plan, User user) {
        this.plan = plan;
        this.user = user;
    }

    public void update(Plan plan) {
        this.plan = plan;
    }

    public void updateAllowedSportSpaces(int allowedSportSpaces) {
        this.allowedSportSpaces = allowedSportSpaces;
    }

}