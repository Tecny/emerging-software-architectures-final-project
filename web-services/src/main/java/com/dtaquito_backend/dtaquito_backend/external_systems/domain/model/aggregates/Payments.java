package com.dtaquito_backend.dtaquito_backend.external_systems.domain.model.aggregates;

import com.dtaquito_backend.dtaquito_backend.shared.domain.model.aggregates.AuditableAbstractAggregateRoot;
import com.dtaquito_backend.dtaquito_backend.users.domain.model.aggregates.User;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Getter
@Setter
public class Payments extends AuditableAbstractAggregateRoot<Payments> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false, foreignKey = @ForeignKey(name = "FK_user_Id_payments"))
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private User user;

    @Column(nullable = false)
    private String transactionId;

    @Column(nullable = false)
    private BigDecimal amount;

    @Column(nullable = false)
    private String currency;

    @Column(nullable = false)
    private String paymentStatus;

    public Payments() {}
}