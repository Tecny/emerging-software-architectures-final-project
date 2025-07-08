package com.dtaquito_backend.dtaquito_backend.bank_transfer.interfaces.rest.transform;

import com.dtaquito_backend.dtaquito_backend.bank_transfer.domain.model.aggregates.BankTransfer;
import com.dtaquito_backend.dtaquito_backend.bank_transfer.interfaces.rest.resources.BankTransferResource;

public class BankTransferResourceFromEntityAssembler {

    public static BankTransferResource toResourceFromEntity(BankTransfer entity) {
        return new BankTransferResource(entity.getId(), entity.getUser().getId(), entity.getFullName(), entity.getBankName(), entity.getTransferType(), entity.getAccountNumber(), entity.getAmount(), entity.getStatus(), entity.getTicketNumber());
    }
}
