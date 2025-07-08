package com.dtaquito_backend.dtaquito_backend.deposit.interfaces.rest;

import com.dtaquito_backend.dtaquito_backend.deposit.domain.model.aggregates.Deposit;
import com.dtaquito_backend.dtaquito_backend.deposit.infrastructure.persistance.jpa.DepositRepository;
import com.dtaquito_backend.dtaquito_backend.iam.application.internal.outboundservices.tokens.TokenService;
import com.dtaquito_backend.dtaquito_backend.iam.infrastructure.tokens.jwt.BearerTokenService;
import com.dtaquito_backend.dtaquito_backend.external_systems.application.internal.commandservices.PayPalPaymentServiceImpl;
import com.dtaquito_backend.dtaquito_backend.external_systems.domain.model.aggregates.Payments;
import com.dtaquito_backend.dtaquito_backend.external_systems.infrastructure.persistance.jpa.PaymentsRepository;
import com.dtaquito_backend.dtaquito_backend.users.domain.model.aggregates.User;
import com.dtaquito_backend.dtaquito_backend.users.infrastructure.persistance.jpa.UserRepository;
import com.paypal.api.payments.Links;
import com.paypal.api.payments.Payment;
import com.paypal.base.rest.PayPalRESTException;
import io.swagger.v3.oas.annotations.Hidden;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@RestController
@Slf4j
@RequestMapping(value="/api/v1/deposit", produces = MediaType.APPLICATION_JSON_VALUE)
public class DepositController {

    private final UserRepository userRepository;
    private final PayPalPaymentServiceImpl payPalPaymentService;
    private final DepositRepository depositRepository;
    private final BearerTokenService bearerTokenService;
    private final TokenService tokenService;
    private final PaymentsRepository paymentsRepository;

    public DepositController(UserRepository userRepository, PayPalPaymentServiceImpl payPalPaymentService, DepositRepository depositRepository, BearerTokenService bearerTokenService,
                             TokenService tokenService, PaymentsRepository paymentsRepository) {
        this.userRepository = userRepository;
        this.payPalPaymentService = payPalPaymentService;
        this.depositRepository = depositRepository;
        this.bearerTokenService = bearerTokenService;
        this.tokenService = tokenService;
        this.paymentsRepository = paymentsRepository;
    }


    public ResponseEntity<Map<String, String>> createDepositPayment(@RequestParam BigDecimal amount, HttpServletRequest request) {
        log.info("Iniciando la creación del pago de depósito por el monto: " + amount);
        try {
            String token = bearerTokenService.getBearerTokenFrom(request);
            if (token == null || !tokenService.validateToken(token)) {
                log.error("Token is not valid");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Token is not valid"));
            }
            String userId = tokenService.getUserIdFromToken(token);

            request.getSession().setAttribute("amount", amount);

            String cancelUrl = "http://localhost:8080/api/v1/deposit/payment-deposits/cancel";
            String successUrl = "http://localhost:8080/api/v1/deposit/payment-deposits/success?jwtToken=" + token;
            Payment payment = payPalPaymentService.createPayment(
                    amount.doubleValue(), "USD", "paypal", "sale", userId, "Depósito de dinero", cancelUrl, successUrl);

            for (Links links : payment.getLinks()) {
                if (links.getRel().equals("approval_url")) {
                    log.info("PayPal approval URL: " + links.getHref());
                    return ResponseEntity.ok(Map.of("approval_url", links.getHref()));
                }
            }
        } catch (PayPalRESTException e) {
            log.error("Error al crear el pago de depósito", e);
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "Error al crear el pago de depósito"));
    }

    @PostMapping("/create-deposit")
    public ResponseEntity<Map<String, String>> createDeposit(@RequestParam BigDecimal amount, HttpServletRequest request) {
        String token = bearerTokenService.getBearerTokenFrom(request);
        if (token == null || !tokenService.validateToken(token)) {
            log.error("Token is not valid");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Token is not valid"));
        }
        String userId = tokenService.getUserIdFromToken(token);

        Optional<User> userOptional = userRepository.findById(Long.parseLong(userId));
        if (userOptional.isEmpty()) {
            log.error("User not found: " + userId);
            return ResponseEntity.status(403).body(Map.of("error", "User not authorized"));
        }

        User user = userOptional.get();
        log.info("User found: " + user.getEmail() + ", Role: " + user.getRole().getRoleType());

        ResponseEntity<Map<String, String>> paymentResponse = createDepositPayment(amount, request);
        if (paymentResponse.getStatusCode() != HttpStatus.OK) {
            return ResponseEntity.status(500).body(Map.of("error", "Failed to generate PayPal payment link"));
        }

        String paymentLink = Objects.requireNonNull(paymentResponse.getBody()).get("approval_url");
        log.info("Generated PayPal payment link: " + paymentLink);

        return ResponseEntity.ok(Map.of("approval_url", paymentLink));
    }


    @Hidden
    @GetMapping("/payment-deposits/success")
    public ResponseEntity<Map<String, String>> paymentSuccess(@RequestParam("paymentId") String paymentId, @RequestParam("PayerID") String payerId, @RequestParam("jwtToken") String jwtToken, HttpServletResponse response) {
        log.info("Payment success callback received with paymentId: {} and PayerID: {}", paymentId, payerId);
        try {
            if (jwtToken == null || !tokenService.validateToken(jwtToken)) {
                log.error("Token is not valid");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Token is not valid"));
            }
            String userId = tokenService.getUserIdFromToken(jwtToken);

            Payment payment = payPalPaymentService.executePayment(paymentId, payerId);
            log.debug("Payment details: {}", payment);

            if (payment.getState().equals("approved")) {
                Optional<User> userOptional = userRepository.findById(Long.parseLong(userId));
                if (userOptional.isEmpty()) {
                    log.error("User not found: " + userId);
                    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "User not found"));
                }

                User user = userOptional.get();
                BigDecimal amount = new BigDecimal(payment.getTransactions().get(0).getAmount().getTotal());

                Deposit deposit = new Deposit();
                deposit.setUser(user);
                deposit.setAmount(amount);
                depositRepository.save(deposit);

                user.setCredits(user.getCredits().add(amount));
                userRepository.save(user);

                Payments transaction = new Payments();
                transaction.setTransactionId(paymentId);
                transaction.setUser(user);
                transaction.setPaymentStatus("APPROVED");
                transaction.setAmount(amount);
                transaction.setCurrency(payment.getTransactions().get(0).getAmount().getCurrency());
                paymentsRepository.save(transaction);

                response.sendRedirect("https://dtaquito-micro.netlify.app/correct-payment");
                return null;
            } else {
                log.error("Payment not approved: " + payment.getState());
                payPalPaymentService.refundPayment(paymentId);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", "Payment not approved"));
            }
        } catch (Exception e) {
            log.error("Error processing payment success callback", e);
            payPalPaymentService.refundPayment(paymentId);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "Error processing payment", "message", e.getMessage()));
        }
    }
}