package de.kaplan.shedlock;

import org.springframework.stereotype.Component;

import java.util.Random;
import java.util.UUID;

@Component
public class PaymentApiClient {

    public String initiatePayment() {
        // Send a request to the external API to initiate the payment
        // For demonstration purposes, return a generated transaction ID
        return UUID.randomUUID().toString();
    }

    public boolean verifyPaymentStatus(String transactionId) {
        // Send a request to the external API to check the payment status
        // For demonstration purposes, return a random verification result
        return new Random().nextBoolean();
    }

    public void completePayment(String transactionId) {
        // Send a request to the external API to complete the payment
        // Assume the payment is completed successfully
    }
}
