package com.dtaquito_backend.dtaquito_backend.deposit.domain.services;

import com.dtaquito_backend.dtaquito_backend.deposit.domain.model.aggregates.Deposit;
import com.dtaquito_backend.dtaquito_backend.deposit.domain.model.queries.GetDepositByIdQuery;

import java.util.Optional;

public interface DepositQueryService {

    Optional<Deposit> handle(GetDepositByIdQuery query);
}
