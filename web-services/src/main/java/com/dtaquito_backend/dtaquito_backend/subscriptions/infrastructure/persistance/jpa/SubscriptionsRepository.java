package com.dtaquito_backend.dtaquito_backend.subscriptions.infrastructure.persistance.jpa;

import com.dtaquito_backend.dtaquito_backend.subscriptions.domain.model.aggregates.Subscription;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SubscriptionsRepository extends JpaRepository<Subscription, Long> {

    List<Subscription> findByUserId(Long userId);
}
