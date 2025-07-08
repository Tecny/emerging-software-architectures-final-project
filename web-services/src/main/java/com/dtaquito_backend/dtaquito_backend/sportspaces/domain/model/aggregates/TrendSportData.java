package com.dtaquito_backend.dtaquito_backend.sportspaces.domain.model.aggregates;

import com.dtaquito_backend.dtaquito_backend.shared.domain.model.aggregates.AuditableAbstractAggregateRoot;
import com.dtaquito_backend.dtaquito_backend.sportspaces.domain.model.commands.CreateTrendSportDataCommand;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.TextStyle;
import java.util.Locale;

@Entity
@Setter
@Getter
@Table(name = "trend_sport_data")
public class TrendSportData extends AuditableAbstractAggregateRoot<TrendSportData> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "sport_space_id", nullable = false)
    private SportSpaces sportSpace;

    @Column(nullable = false)
    private String openingHour;

    @Column(nullable = false)
    private String amountPeople;

    @Column(nullable = false)
    private String currentMonth;

    @Column(nullable = false)
    private String currentYear;

    protected TrendSportData() {}

    public TrendSportData(CreateTrendSportDataCommand command) {
        this.openingHour = command.openingHour();
        this.amountPeople = command.amountPeople();
        this.currentMonth = command.currentMonth();
        this.currentYear = command.currentYear();
    }

    @PrePersist
    public void prePersist() {
        if (this.currentMonth == null) {
            ZonedDateTime nowInLima = ZonedDateTime.now(ZoneId.of("America/Lima"));
            this.currentMonth = nowInLima.getMonth().getDisplayName(TextStyle.FULL, new Locale("es"));
        }
        if(this.currentYear == null) {
            ZonedDateTime nowInLima = ZonedDateTime.now(ZoneId.of("America/Lima"));
            this.currentYear = String.valueOf(nowInLima.getYear());
        }
    }
}
