package com.dtaquito_backend.dtaquito_backend.users.domain.model.entities;

import com.dtaquito_backend.dtaquito_backend.users.domain.model.valueObjects.RoleTypes;
import jakarta.persistence.*;
import lombok.Getter;

@Entity
@Getter
@Table(name = "role_types")
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private RoleTypes roleType;

    public Role() {}

    public static Role fromNameToRole(String name) {
        return new Role(RoleTypes.valueOf(name));
    }

    public Role(RoleTypes roleType) {
        this.roleType = roleType;
    }
}