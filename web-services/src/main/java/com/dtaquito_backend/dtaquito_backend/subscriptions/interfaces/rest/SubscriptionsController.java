package com.dtaquito_backend.dtaquito_backend.subscriptions.interfaces.rest;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Map;

import com.dtaquito_backend.dtaquito_backend.iam.infrastructure.authorization.sfs.model.UserDetailsImpl;
import com.dtaquito_backend.dtaquito_backend.external_systems.application.internal.commandservices.PayPalPaymentServiceImpl;
import com.dtaquito_backend.dtaquito_backend.external_systems.domain.model.aggregates.Payments;
import com.dtaquito_backend.dtaquito_backend.external_systems.infrastructure.persistance.jpa.PaymentsRepository;
import com.dtaquito_backend.dtaquito_backend.subscriptions.domain.model.entities.Plan;
import com.dtaquito_backend.dtaquito_backend.subscriptions.domain.model.valueObjects.PlanTypes;
import com.dtaquito_backend.dtaquito_backend.subscriptions.infrastructure.persistance.jpa.PlanRepository;
import com.dtaquito_backend.dtaquito_backend.subscriptions.infrastructure.persistance.jpa.SubscriptionsRepository;
import com.dtaquito_backend.dtaquito_backend.subscriptions.interfaces.rest.transform.SubscriptionsResourceFromEntityAssembler;
import com.dtaquito_backend.dtaquito_backend.users.domain.model.aggregates.User;
import com.dtaquito_backend.dtaquito_backend.users.domain.services.UserQueryService;
import com.dtaquito_backend.dtaquito_backend.users.infrastructure.persistance.jpa.UserRepository;
import com.paypal.api.payments.Links;
import com.paypal.api.payments.Payment;
import com.paypal.base.rest.PayPalRESTException;
import io.swagger.v3.oas.annotations.Hidden;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.dtaquito_backend.dtaquito_backend.subscriptions.domain.model.aggregates.Subscription;
import com.dtaquito_backend.dtaquito_backend.subscriptions.domain.services.SubscriptionsQueryService;

import java.util.Optional;

@RestController
@Slf4j
@RequestMapping(value = "/api/v1/subscriptions", produces = MediaType.APPLICATION_JSON_VALUE)
public class SubscriptionsController {

    private final SubscriptionsQueryService subscriptionsQueryService;
    private final PlanRepository planRepository;
    private final PayPalPaymentServiceImpl payPalPaymentService;
    private final PaymentsRepository paymentsRepository;
    private final SubscriptionsRepository subscriptionsRepository;
    private final UserQueryService userQueryService;
    private final UserRepository userRepository;

    public SubscriptionsController(SubscriptionsQueryService subscriptionsQueryService, PlanRepository planRepository, PayPalPaymentServiceImpl payPalPaymentService, PaymentsRepository paymentsRepository,
                                   SubscriptionsRepository subscriptionsRepository, UserQueryService userQueryService, UserRepository userRepository) {
        this.subscriptionsQueryService = subscriptionsQueryService;
        this.planRepository = planRepository;
        this.payPalPaymentService = payPalPaymentService;
        this.paymentsRepository = paymentsRepository;
        this.subscriptionsRepository = subscriptionsRepository;
        this.userQueryService = userQueryService;
        this.userRepository = userRepository;
    }

    @PutMapping("/upgrade")
    public ResponseEntity<Map<String, String>> upgradeSubscription(@RequestParam PlanTypes newPlanType, Authentication authentication) {
        try {
            if (authentication == null || authentication.getPrincipal() == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Unauthorized"));
            }

            UserDetailsImpl principal = (UserDetailsImpl) authentication.getPrincipal();
            Long userId = principal.getId();
            ZonedDateTime now = ZonedDateTime.now(ZoneId.of("America/Lima"));
            int hour = now.getHour();

//            if (now.getDayOfWeek() != DayOfWeek.MONDAY || hour > 6) {
//                return ResponseEntity.status(HttpStatus.FORBIDDEN)
//                        .body(Map.of("error", "Upgrades can only be performed on Mondays from 00:00 to 06:00 (America/Lima)"));
//            }

            String userRole = userQueryService.getUserRoleByUserId(userId);

            if ("ADMIN".equals(userRole) || "PLAYER".equals(userRole)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "Only owners can upgrade their subscription plan"));
            }

            Subscription subscription = subscriptionsQueryService.getSubscriptionByUserId(userId)
                    .orElseThrow(() -> new IllegalArgumentException("Subscription not found"));

            PlanTypes currentPlanType = subscription.getPlan().getPlanType();
            if (currentPlanType == PlanTypes.GOLD) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", "User is already at the highest plan level"));
            }
            if ((currentPlanType == PlanTypes.BRONZE && newPlanType != PlanTypes.SILVER && newPlanType != PlanTypes.GOLD) ||
                    (currentPlanType == PlanTypes.SILVER && newPlanType != PlanTypes.GOLD)) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", "Invalid upgrade path"));
            }

            BigDecimal amount;
            switch (newPlanType) {
                case GOLD:
                    amount = new BigDecimal("14.99");
                    break;
                case SILVER:
                    amount = new BigDecimal("9.99");
                    break;
                case BRONZE:
                    amount = new BigDecimal("4.99");
                    break;
                default:
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", "Invalid plan type"));
            }

            String cancelUrl = "http://localhost:8080/api/v1/subscriptions/payment/cancel";
            String successUrl = "http://localhost:8080/api/v1/subscriptions/payment/success";
            Payment payment = payPalPaymentService.createPayment(
                    amount.doubleValue(), "USD", "paypal", "sale", userId.toString(), "Subscription upgrade", cancelUrl, successUrl);

            Optional<User> userOptional = userRepository.findById(userId);
            if (userOptional.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "User not found"));
            }
            User user = userOptional.get();

            Payments transaction = new Payments();
            transaction.setTransactionId(payment.getId());
            transaction.setUser(user);
            transaction.setPaymentStatus("PENDING");
            transaction.setAmount(amount);
            transaction.setCurrency("USD");
            paymentsRepository.save(transaction);

            for (Links links : payment.getLinks()) {
                if (links.getRel().equals("approval_url")) {
                    return ResponseEntity.ok(Map.of("approval_url", links.getHref()));
                }
            }

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "Approval URL not found"));
        } catch (PayPalRESTException e) {
            log.error("Error processing PayPal payment", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "Error processing payment"));
        } catch (Exception e) {
            log.error("Error upgrading subscription", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "Error upgrading subscription"));
        }
    }

    @Hidden
    @GetMapping("/payment/success")
    public void upgradeSuccess(@RequestParam("paymentId") String paymentId,
                               @RequestParam("PayerID") String payerId,
                               HttpServletResponse response) {
        log.info("Upgrade success callback received with paymentId: {} and PayerID: {}", paymentId, payerId);
        try {

            Payment payment = payPalPaymentService.executePayment(paymentId, payerId);
            log.debug("Payment details: {}", payment);

            if (payment.getState().equals("approved")) {
                Payments transaction = paymentsRepository.findByTransactionId(paymentId)
                        .orElseThrow(() -> new IllegalArgumentException("Transaction not found"));

                Long userId = Long.parseLong(transaction.getUser().getId().toString());

                Subscription subscription = subscriptionsQueryService.getSubscriptionByUserId(userId)
                        .orElseThrow(() -> new IllegalArgumentException("Subscription not found"));
                
                PlanTypes newPlanType;
                int allowedSportSpaces;
                BigDecimal amount = transaction.getAmount();
                if (amount.equals(new BigDecimal("14.99"))) {
                    newPlanType = PlanTypes.GOLD;
                    allowedSportSpaces = 3;
                } else if (amount.equals(new BigDecimal("9.99"))) {
                    newPlanType = PlanTypes.SILVER;
                    allowedSportSpaces = 2;
                } else if (amount.equals(new BigDecimal("4.99"))) {
                    newPlanType = PlanTypes.BRONZE;
                    allowedSportSpaces = 1;
                } else {
                    newPlanType = PlanTypes.FREE;
                    allowedSportSpaces = 0;
                }
                Plan plan = planRepository.findByPlanType(newPlanType)
                        .orElseThrow(() -> new IllegalArgumentException("Plan not found"));

                subscription.updateAllowedSportSpaces(allowedSportSpaces);
                subscription.update(plan);
                subscriptionsRepository.save(subscription);

                transaction.setPaymentStatus("APPROVED");
                paymentsRepository.save(transaction);
                response.sendRedirect("https://dtaquito-micro.netlify.app/correct-payment");
            } else {
                log.error("Payment state is not approved: {}", payment.getState());
                payPalPaymentService.refundPayment(paymentId);
                response.sendRedirect("https://dtaquito-micro.netlify.app/error-payment");
            }
        } catch (Exception e) {
            log.error("Error processing upgrade success callback", e);
            try {
                response.sendRedirect("https://dtaquito-micro.netlify.app/error-payment");
            } catch (IOException ioException) {
                log.error("Error redirecting to error-payment page", ioException);
            }
        }
    }

    @Hidden
    @GetMapping("/payment/cancel")
    public String upgradeCancel(@RequestParam("paymentId") String paymentId) {
        log.info("Upgrade cancelled with paymentId: {}", paymentId);

        Payments transaction = new Payments();
        transaction.setTransactionId(paymentId);
        transaction.setPaymentStatus("CANCELLED");
        paymentsRepository.save(transaction);

        return "upgradeCancel";
    }

    @GetMapping()
    public ResponseEntity<?> getSubscriptionByUserId(Authentication authentication) {

        if (authentication == null || authentication.getPrincipal() == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
        }
        UserDetailsImpl principal = (UserDetailsImpl) authentication.getPrincipal();
        Long userId = principal.getId();
        Optional<Subscription> subscription = subscriptionsQueryService.getSubscriptionByUserId(userId);
        if (subscription.isPresent()) {
            return ResponseEntity.ok(SubscriptionsResourceFromEntityAssembler.toResourceFromEntity(subscription.get()));
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}