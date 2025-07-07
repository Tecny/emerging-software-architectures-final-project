package com.dtaquito_backend.dtaquito_backend.deposit.domain.services;

import com.dtaquito_backend.dtaquito_backend.deposit.domain.model.aggregates.Deposit;
import com.dtaquito_backend.dtaquito_backend.deposit.domain.model.commands.CreateDepositCommand;

import java.util.Optional;

public interface DepositCommandService {

    Optional<Deposit> handle(CreateDepositCommand command);
    void save(Deposit deposit);
}
