package com.dtaquito_backend.dtaquito_backend.deposit.application.internal.queryservices;

import com.dtaquito_backend.dtaquito_backend.deposit.domain.model.aggregates.Deposit;
import com.dtaquito_backend.dtaquito_backend.deposit.domain.model.queries.GetDepositByIdQuery;
import com.dtaquito_backend.dtaquito_backend.deposit.domain.services.DepositQueryService;
import com.dtaquito_backend.dtaquito_backend.deposit.infrastructure.persistance.jpa.DepositRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class DepositQueryServiceImpl implements DepositQueryService {

    private final DepositRepository depositRepository;

    public DepositQueryServiceImpl(DepositRepository depositRepository) {
        this.depositRepository = depositRepository;
    }

    @Override
    public Optional<Deposit> handle(GetDepositByIdQuery query) {
        return depositRepository.findById(query.getDepositId());
    }
}