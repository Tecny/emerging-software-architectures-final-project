package com.dtaquito_backend.dtaquito_backend.bank_transfer.interfaces.rest.resources;

public record CreateBankTransferResource(String fullName, String bankName, String transferType, String accountNumber) {

    public CreateBankTransferResource {

        if (fullName == null) {
            throw new IllegalArgumentException("FullName cannot be null");
        }
        if (bankName == null) {
            throw new IllegalArgumentException("BankName cannot be null");
        }
        if (transferType == null) {
            throw new IllegalArgumentException("TransferType cannot be null");
        }
        if (accountNumber == null) {
            throw new IllegalArgumentException("AccountNumber cannot be null");
        }
    }
}
