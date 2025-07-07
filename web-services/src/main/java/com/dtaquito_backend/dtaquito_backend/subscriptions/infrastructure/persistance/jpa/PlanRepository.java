package com.dtaquito_backend.dtaquito_backend.subscriptions.infrastructure.persistance.jpa;

import com.dtaquito_backend.dtaquito_backend.subscriptions.domain.model.entities.Plan;
import com.dtaquito_backend.dtaquito_backend.subscriptions.domain.model.valueObjects.PlanTypes;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PlanRepository extends JpaRepository<Plan, Long>{

    boolean existsByPlanType(PlanTypes planType);
    Optional<Plan> findByPlanType(PlanTypes planType);

}