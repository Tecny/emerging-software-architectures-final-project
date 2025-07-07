package com.dtaquito_backend.dtaquito_backend.users.infrastructure.persistance.jpa;

import com.dtaquito_backend.dtaquito_backend.users.domain.model.aggregates.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmailAndPassword(String email, String password);

    Optional<User> findByEmail(String email);
}