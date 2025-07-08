package com.dtaquito_backend.dtaquito_backend.external_systems.application.internal.queryservices;

import java.util.Optional;

import com.dtaquito_backend.dtaquito_backend.external_systems.domain.model.aggregates.Payments;
import com.dtaquito_backend.dtaquito_backend.external_systems.domain.model.queries.GetPaymentsByIdQuery;
import com.dtaquito_backend.dtaquito_backend.external_systems.domain.services.PaymentsQueryService;
import com.dtaquito_backend.dtaquito_backend.external_systems.infrastructure.persistance.jpa.PaymentsRepository;
import org.springframework.stereotype.Service;

@Service
public class PaymentsQueryServiceImpl implements PaymentsQueryService {

    private final PaymentsRepository paymentsRepository;

    public PaymentsQueryServiceImpl(PaymentsRepository paymentsRepository) {
        this.paymentsRepository = paymentsRepository;
    }

    @Override
    public Optional<Payments> handle(GetPaymentsByIdQuery query) {
        return paymentsRepository.findById(query.id());
    }
}
