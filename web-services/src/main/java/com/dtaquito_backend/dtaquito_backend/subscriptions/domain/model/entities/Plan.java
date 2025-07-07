package com.dtaquito_backend.dtaquito_backend.subscriptions.domain.model.entities;

import com.dtaquito_backend.dtaquito_backend.subscriptions.domain.model.valueObjects.PlanTypes;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "plan_types")
public class Plan  {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private PlanTypes planType;

    public Plan() {}

    public Plan(PlanTypes planType) {
        this.planType = planType;
    }
}
