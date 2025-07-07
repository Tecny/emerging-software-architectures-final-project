package com.dtaquito_backend.dtaquito_backend.users.infrastructure.persistance.jpa;

import com.dtaquito_backend.dtaquito_backend.users.domain.model.entities.Role;
import com.dtaquito_backend.dtaquito_backend.users.domain.model.valueObjects.RoleTypes;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {

    boolean existsByRoleType(RoleTypes roleType);
    Optional<Role> findByRoleType(RoleTypes roleType);
}
