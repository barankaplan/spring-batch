package de.kaplan.shedlock;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.stereotype.Service;

import java.util.function.Supplier;

@Service
public class VerifyPaymentTasklet implements Tasklet {

    private static final Logger logger = LoggerFactory.getLogger(VerifyPaymentTasklet.class);

    private final PaymentApiClient paymentApiClient;
    private final Retry retry;
    private final CircuitBreaker circuitBreaker;

    public VerifyPaymentTasklet(PaymentApiClient paymentApiClient,
                                RetryRegistry retryRegistry,
                                CircuitBreakerRegistry circuitBreakerRegistry) {
        this.paymentApiClient = paymentApiClient;
        this.retry = retryRegistry.retry("verifyPaymentRetry");
        this.circuitBreaker = circuitBreakerRegistry.circuitBreaker("verifyPaymentCircuitBreaker");

        retry.getEventPublisher().onRetry(event ->
                logger.info("Retry attempt {} for verifyPayment", event.getNumberOfRetryAttempts()));

        circuitBreaker.getEventPublisher()
                .onSuccess(event -> logger.info("CircuitBreaker success on attempt {}", event.getElapsedDuration()))
                .onError(event -> logger.warn("CircuitBreaker error: {}", event.getThrowable().getMessage()))
                .onStateTransition(event -> logger.info("CircuitBreaker state transition: from {} to {}",
                        event.getStateTransition().getFromState(), event.getStateTransition().getToState()));
    }

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        logger.info("VerifyPaymentTasklet - Starting payment verification...");

        String transactionId = (String) chunkContext.getStepContext().getJobExecutionContext().get("transactionId");

        Supplier<Boolean> verifyPaymentSupplier = CircuitBreaker.decorateSupplier(circuitBreaker,
                Retry.decorateSupplier(retry, () -> {
                    boolean isVerified = paymentApiClient.verifyPaymentStatus(transactionId);
                    if (!isVerified) {
                        throw new RuntimeException("Payment verification failed.");
                    }
                    return isVerified;
                }));

        boolean isVerified = verifyPaymentSupplier.get();

        if (isVerified) {
            logger.info("VerifyPaymentTasklet - Payment successfully verified.");
            return RepeatStatus.FINISHED;
        } else {
            logger.warn("VerifyPaymentTasklet - Payment verification ultimately failed after retries.");
            throw new RuntimeException("Payment verification ultimately failed.");
        }
    }
}
