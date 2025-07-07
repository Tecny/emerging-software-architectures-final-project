package com.dtaquito_backend.dtaquito_backend.deposit.domain.model.commands;

import java.math.BigDecimal;

public record CreateDepositCommand(Long userId, Long packId, BigDecimal amount) {
}