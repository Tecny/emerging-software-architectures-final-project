package com.dtaquito_backend.dtaquito_backend.external_systems.application.internal.commandservices;

import com.paypal.api.payments.*;
import com.paypal.base.rest.APIContext;
import com.paypal.base.rest.PayPalRESTException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
@Slf4j
public class PayPalPaymentServiceImpl {

    private final APIContext apiContext;

    public Payment createPayment(
            Double total, String currency, String method,
            String intent, String customField, String description, String cancelUrl, String successUrl) throws PayPalRESTException {

        Amount amount = new Amount();
        amount.setCurrency(currency);
        amount.setTotal(String.format(Locale.forLanguageTag(currency), "%.2f", total));

        Transaction transaction = new Transaction();
        transaction.setDescription(description);
        transaction.setAmount(amount);
        transaction.setCustom(customField);

        List<Transaction> transactions = new ArrayList<>();
        transactions.add(transaction);

        Payer payer = new Payer();
        payer.setPaymentMethod(method);

        Payment payment = new Payment();
        payment.setIntent(intent);
        payment.setPayer(payer);
        payment.setTransactions(transactions);

        RedirectUrls redirectUrls = new RedirectUrls();
        redirectUrls.setCancelUrl(cancelUrl);
        redirectUrls.setReturnUrl(successUrl);

        payment.setRedirectUrls(redirectUrls);

        return payment.create(apiContext);
    }

    public Payment executePayment(String paymentId, String payerId) throws PayPalRESTException {
        Payment payment = new Payment();
        payment.setId(paymentId);
        PaymentExecution paymentExecution = new PaymentExecution();
        paymentExecution.setPayerId(payerId);
        return payment.execute(apiContext, paymentExecution);
    }

    public void refundPayment(String paymentId) {
        try {
            Payment payment = Payment.get(apiContext, paymentId);
            if (payment != null) {
                for (Transaction transaction : payment.getTransactions()) {
                    for (RelatedResources resource : transaction.getRelatedResources()) {
                        Sale sale = resource.getSale();
                        if (sale != null) {
                            RefundRequest refundRequest = new RefundRequest();
                            sale.refund(apiContext, refundRequest);
                            log.info("Payment with ID {} has been refunded", paymentId);
                            return;
                        }
                    }
                }
            }
        } catch (PayPalRESTException e) {
            log.error("Error refunding payment with ID {}", paymentId, e);
        }
    }
}