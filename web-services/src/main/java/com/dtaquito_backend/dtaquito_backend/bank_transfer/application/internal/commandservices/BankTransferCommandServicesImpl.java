package com.dtaquito_backend.dtaquito_backend.bank_transfer.application.internal.commandservices;

import com.dtaquito_backend.dtaquito_backend.bank_transfer.domain.model.aggregates.BankTransfer;
import com.dtaquito_backend.dtaquito_backend.bank_transfer.domain.model.commands.CreateBankTransferCommand;
import com.dtaquito_backend.dtaquito_backend.bank_transfer.domain.services.BankTransferCommandServices;
import com.dtaquito_backend.dtaquito_backend.users.infrastructure.persistance.jpa.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@Slf4j
public class BankTransferCommandServicesImpl implements BankTransferCommandServices {

    private final UserRepository userRepository;

    public BankTransferCommandServicesImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public Optional<BankTransfer> handle(Long id, CreateBankTransferCommand command) {

        var user = userRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("User not found"));
        var bankTransfer = new BankTransfer(command, user);
        return Optional.of(bankTransfer);
    }
}
