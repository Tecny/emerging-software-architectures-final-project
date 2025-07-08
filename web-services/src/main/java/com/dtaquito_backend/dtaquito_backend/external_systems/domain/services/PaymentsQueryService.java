package com.dtaquito_backend.dtaquito_backend.external_systems.domain.services;

import java.util.Optional;

import com.dtaquito_backend.dtaquito_backend.external_systems.domain.model.aggregates.Payments;
import com.dtaquito_backend.dtaquito_backend.external_systems.domain.model.queries.GetPaymentsByIdQuery;


public interface PaymentsQueryService {

    Optional<Payments> handle(GetPaymentsByIdQuery query);
}
