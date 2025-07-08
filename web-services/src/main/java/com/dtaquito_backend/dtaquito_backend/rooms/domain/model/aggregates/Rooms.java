package com.dtaquito_backend.dtaquito_backend.rooms.domain.model.aggregates;

import com.dtaquito_backend.dtaquito_backend.player_list.domain.model.aggregates.PlayerList;
import com.dtaquito_backend.dtaquito_backend.reservations.domain.model.aggregates.Reservations;
import com.dtaquito_backend.dtaquito_backend.shared.domain.model.aggregates.AuditableAbstractAggregateRoot;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Setter
@Getter
public class Rooms extends AuditableAbstractAggregateRoot<Rooms> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToMany(mappedBy = "room", fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    private List<PlayerList> playerLists = new ArrayList<>();

    @ManyToOne
    @JoinColumn(name = "reservations_id", nullable = false)
    private Reservations reservations;

    @Column(name = "accumulated_amount", nullable = false)
    private BigDecimal accumulatedAmount = BigDecimal.ZERO;

    public Rooms() {}
}