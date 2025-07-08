package com.dtaquito_backend.dtaquito_backend.deposit.application.internal.commandservices;

import com.dtaquito_backend.dtaquito_backend.deposit.domain.model.aggregates.Deposit;
import com.dtaquito_backend.dtaquito_backend.deposit.domain.model.commands.CreateDepositCommand;
import com.dtaquito_backend.dtaquito_backend.deposit.domain.services.DepositCommandService;
import com.dtaquito_backend.dtaquito_backend.deposit.infrastructure.persistance.jpa.DepositRepository;
import com.dtaquito_backend.dtaquito_backend.users.infrastructure.persistance.jpa.UserRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Optional;

@Service
public class DepositCommandServiceImpl implements DepositCommandService {

    private final DepositRepository depositRepository;
    private final UserRepository userRepository;

    public DepositCommandServiceImpl(DepositRepository depositRepository, UserRepository userRepository) {
        this.depositRepository = depositRepository;
        this.userRepository = userRepository;
    }

    @Override
    public Optional<Deposit> handle(CreateDepositCommand command) {
        var user = userRepository.findById(command.userId())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        BigDecimal amount = command.amount();

        var deposit = new Deposit(user, amount);
        var createdDeposit = depositRepository.save(deposit);

        return Optional.of(createdDeposit);
    }


    @Override
    public void save(Deposit deposit) {
        depositRepository.save(deposit);
    }
}