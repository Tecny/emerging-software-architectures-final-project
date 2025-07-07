package com.dtaquito_backend.dtaquito_backend.users.domain.model.aggregates;

import com.dtaquito_backend.dtaquito_backend.shared.domain.model.aggregates.AuditableAbstractAggregateRoot;
import com.dtaquito_backend.dtaquito_backend.users.domain.model.entities.Role;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import com.dtaquito_backend.dtaquito_backend.users.domain.model.commands.CreateUserCommand;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Getter
@Setter
public class User extends AuditableAbstractAggregateRoot<User> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false)
    private String name;

    @NotBlank
    @Column(nullable = false)
    private String email;

    @NotBlank
    @Column(nullable = false)
    private String password;

    @ManyToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinColumn(name = "role_id", nullable = false)
    private Role role;

    @Column(nullable = false)
    private BigDecimal credits = BigDecimal.ZERO;

    protected User() {}

    public User(CreateUserCommand command, Role role) {
        this.name = command.name();
        this.email = command.email();
        this.password = command.password();
        this.role = role;
    }

    public User(Long id, String name, String email, String password, Role role, BigDecimal credits) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.password = password;
        this.role = role;
        this.credits = credits;
    }

    public User(String name, String email, String password, Role role, BigDecimal credits) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.role = role;
        this.credits = credits;
    }
}