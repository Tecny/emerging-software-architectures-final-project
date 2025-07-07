package com.dtaquito_backend.dtaquito_backend.bank_transfer.domain.model.commands;

public record CreateBankTransferCommand(String fullName, String bankName, String transferType, String accountNumber) {
    public CreateBankTransferCommand {
        if (fullName == null || fullName.isBlank()) {
            throw new IllegalArgumentException("Full name is required");
        }
        if (bankName == null || bankName.isBlank()) {
            throw new IllegalArgumentException("Bank name is required");
        }
        if (transferType == null || transferType.isBlank()) {
            throw new IllegalArgumentException("Transfer type is required");
        }
        if (accountNumber == null || accountNumber.isBlank()) {
            throw new IllegalArgumentException("Account number is required");
        }
    }
}
