package com.dtaquito_backend.dtaquito_backend.deposit.infrastructure.persistance.jpa;

import com.dtaquito_backend.dtaquito_backend.deposit.domain.model.aggregates.Deposit;
import org.springframework.data.jpa.repository.JpaRepository;


public interface DepositRepository extends JpaRepository<Deposit, Long> {}