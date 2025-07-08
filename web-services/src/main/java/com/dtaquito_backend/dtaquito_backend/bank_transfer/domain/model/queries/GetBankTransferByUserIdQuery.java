package com.dtaquito_backend.dtaquito_backend.bank_transfer.domain.model.queries;

public record GetBankTransferByUserIdQuery(Long userId) {
    public GetBankTransferByUserIdQuery {
        if (userId == null) {
            throw new IllegalArgumentException("UserId is required");
        }
    }
}

