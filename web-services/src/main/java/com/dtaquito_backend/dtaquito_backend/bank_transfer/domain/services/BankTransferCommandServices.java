package com.dtaquito_backend.dtaquito_backend.bank_transfer.domain.services;

import com.dtaquito_backend.dtaquito_backend.bank_transfer.domain.model.aggregates.BankTransfer;
import com.dtaquito_backend.dtaquito_backend.bank_transfer.domain.model.commands.CreateBankTransferCommand;

import java.util.Optional;

public interface BankTransferCommandServices {

    Optional<BankTransfer> handle(Long id, CreateBankTransferCommand command);

}
