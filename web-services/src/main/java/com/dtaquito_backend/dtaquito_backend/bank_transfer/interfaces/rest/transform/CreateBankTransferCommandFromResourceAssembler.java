package com.dtaquito_backend.dtaquito_backend.bank_transfer.interfaces.rest.transform;

import com.dtaquito_backend.dtaquito_backend.bank_transfer.domain.model.commands.CreateBankTransferCommand;
import com.dtaquito_backend.dtaquito_backend.bank_transfer.interfaces.rest.resources.CreateBankTransferResource;

public class CreateBankTransferCommandFromResourceAssembler {

    public static CreateBankTransferCommand toCommandFromResource(CreateBankTransferResource resource) {
        return new CreateBankTransferCommand(
                resource.fullName(),
                resource.bankName(),
                resource.transferType(),
                resource.accountNumber()
        );
    }
}
