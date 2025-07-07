package com.dtaquito_backend.dtaquito_backend.deposit.domain.model.aggregates;

import com.dtaquito_backend.dtaquito_backend.shared.domain.model.aggregates.AuditableAbstractAggregateRoot;
import com.dtaquito_backend.dtaquito_backend.users.domain.model.aggregates.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;

@Entity
@Table(name = "deposits")
@Getter
@Setter
@EntityListeners(AuditingEntityListener.class)
public class Deposit extends AuditableAbstractAggregateRoot<Deposit> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private BigDecimal amount;

    public Deposit() {
    }

    public Deposit(User user, BigDecimal amount) {
        this.user = user;
        this.amount = amount;
    }
}