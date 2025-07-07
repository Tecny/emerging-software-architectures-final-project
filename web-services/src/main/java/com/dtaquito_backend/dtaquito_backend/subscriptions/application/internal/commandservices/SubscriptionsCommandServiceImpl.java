package com.dtaquito_backend.dtaquito_backend.subscriptions.application.internal.commandservices;

import com.dtaquito_backend.dtaquito_backend.external_systems.application.internal.commandservices.PayPalPaymentServiceImpl;
import com.dtaquito_backend.dtaquito_backend.subscriptions.domain.model.aggregates.Subscription;
import com.dtaquito_backend.dtaquito_backend.subscriptions.domain.model.commands.CreateSubscriptionsCommand;
import com.dtaquito_backend.dtaquito_backend.subscriptions.domain.model.entities.Plan;
import com.dtaquito_backend.dtaquito_backend.subscriptions.domain.model.events.SubscriptionCreatedEvent;
import com.dtaquito_backend.dtaquito_backend.subscriptions.domain.model.valueObjects.PlanTypes;
import com.dtaquito_backend.dtaquito_backend.subscriptions.domain.services.SubscriptionsCommandService;
import com.dtaquito_backend.dtaquito_backend.subscriptions.infrastructure.persistance.jpa.PlanRepository;
import com.dtaquito_backend.dtaquito_backend.subscriptions.infrastructure.persistance.jpa.SubscriptionsRepository;
import com.dtaquito_backend.dtaquito_backend.users.infrastructure.persistance.jpa.UserRepository;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class SubscriptionsCommandServiceImpl implements SubscriptionsCommandService {

    private final SubscriptionsRepository subscriptionsRepository;
    private final UserRepository userRepository;
    private final PlanRepository planRepository;
    private final ApplicationEventPublisher applicationEventPublisher;

    public SubscriptionsCommandServiceImpl(SubscriptionsRepository subscriptionsRepository, UserRepository userRepository, PlanRepository planRepository, ApplicationEventPublisher applicationEventPublisher, PayPalPaymentServiceImpl payPalPaymentServiceImpl) {
        this.subscriptionsRepository = subscriptionsRepository;
        this.userRepository = userRepository;
        this.planRepository = planRepository;
        this.applicationEventPublisher = applicationEventPublisher;
    }

    @Override
    public Optional<Subscription> handle(CreateSubscriptionsCommand command) {

        var user = userRepository.findById(command.userId())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        Plan plan = planRepository.findById(command.planId())
                .orElseGet(() -> planRepository.findByPlanType(PlanTypes.FREE)
                        .orElseThrow(() -> new IllegalArgumentException("Free plan not found")));

        List<Subscription> existingSubscriptions = subscriptionsRepository.findByUserId(user.getId());

        if (!existingSubscriptions.isEmpty()) {
            for (Subscription subscription : existingSubscriptions) {
                subscription.setPlan(plan);
                subscriptionsRepository.save(subscription);
            }
            return Optional.of(existingSubscriptions.get(0));
        }

        var subscriptions = new Subscription(plan, user);
        var createdSubscriptions = subscriptionsRepository.save(subscriptions);

        SubscriptionCreatedEvent event = new SubscriptionCreatedEvent(this, createdSubscriptions.getId());
        applicationEventPublisher.publishEvent(event);
        return Optional.of(createdSubscriptions);
    }

    @Override
    public void handleSubscriptionCreatedEvent(SubscriptionCreatedEvent event) {
        System.out.println("SuscriptionDeletedEvent received for subscription ID: " + event.getSubscriptionId());
    }
}