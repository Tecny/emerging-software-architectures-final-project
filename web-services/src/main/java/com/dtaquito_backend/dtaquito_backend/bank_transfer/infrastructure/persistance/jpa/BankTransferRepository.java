package com.dtaquito_backend.dtaquito_backend.bank_transfer.infrastructure.persistance.jpa;

import com.dtaquito_backend.dtaquito_backend.bank_transfer.domain.model.aggregates.BankTransfer;
import com.dtaquito_backend.dtaquito_backend.reservations.domain.model.valueobjects.Status;
import org.springframework.data.jpa.repository.JpaRepository;


import java.util.List;
import java.util.Optional;

public interface BankTransferRepository extends JpaRepository<BankTransfer, Long> {

    List<BankTransfer> findByUserId(Long userId);

    Optional<BankTransfer> findByUserIdAndStatus(Long userId, Status status);
}
