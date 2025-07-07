package com.dtaquito_backend.dtaquito_backend.bank_transfer.interfaces.rest.resources;

import com.dtaquito_backend.dtaquito_backend.reservations.domain.model.valueobjects.Status;

public record BankTransferResource(Long id, Long userId, String fullName, String bankName, String transferType, String accountNumber, Long amount, Status status, String ticketNumber) {

    public BankTransferResource {
        if (id == null) {
            throw new IllegalArgumentException("Id cannot be null");
        }
        if (userId == null) {
            throw new IllegalArgumentException("UserId cannot be null");
        }
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
        if (amount == null) {
            throw new IllegalArgumentException("Amount cannot be null");
        }
        if (status == null) {
            throw new IllegalArgumentException("Status cannot be null");
        }
        if (ticketNumber == null) {
            throw new IllegalArgumentException("TicketNumber cannot be null");
        }
    }
}
