package com.dtaquito_backend.dtaquito_backend.external_systems.infrastructure.persistance.jpa;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import com.dtaquito_backend.dtaquito_backend.external_systems.domain.model.aggregates.Payments;

public interface PaymentsRepository extends JpaRepository<Payments, Long> {

    Optional<Payments> findByTransactionId(String transactionId);
}