package com.dtaquito_backend.dtaquito_backend.bank_transfer.domain.model.aggregates;

import com.dtaquito_backend.dtaquito_backend.bank_transfer.domain.model.commands.CreateBankTransferCommand;
import com.dtaquito_backend.dtaquito_backend.reservations.domain.model.valueobjects.Status;
import com.dtaquito_backend.dtaquito_backend.shared.domain.model.aggregates.AuditableAbstractAggregateRoot;
import com.dtaquito_backend.dtaquito_backend.users.domain.model.aggregates.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.util.UUID;

@Entity
@Setter
@Getter
@EntityListeners(AuditingEntityListener.class)
public class BankTransfer extends AuditableAbstractAggregateRoot<BankTransfer> {

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", foreignKey = @ForeignKey(name = "fk_bank_transfer_user"))
    private User user;

    @Column(nullable = false)
    private String fullName;

    @Column(nullable = false)
    private String bankName;

    @Column(nullable = false)
    private String transferType;

    @Column(nullable = false)
    private String accountNumber;

    @Column(nullable = false)
    private Long amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Status status;

    @Column(nullable = false)
    private String ticketNumber;

    @PrePersist
    public void prePersist() {
        this.ticketNumber = UUID.randomUUID().toString();
        this.status = Status.PENDING;
    }

    protected BankTransfer() {}

    public BankTransfer(CreateBankTransferCommand command, User user){

        this.fullName = command.fullName();
        this.accountNumber = command.accountNumber();
        this.transferType = command.transferType();
        this.bankName = command.bankName();
        this.user = user;
    }
}
