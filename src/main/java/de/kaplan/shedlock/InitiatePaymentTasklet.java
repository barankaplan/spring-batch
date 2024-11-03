package de.kaplan.shedlock;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.stereotype.Service;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

@Service
public class InitiatePaymentTasklet implements org.springframework.batch.core.step.tasklet.Tasklet {

    private static final Logger logger = LoggerFactory.getLogger(InitiatePaymentTasklet.class);

    private final PaymentApiClient paymentApiClient;
    private final Retry retry;
    private final CircuitBreaker circuitBreaker;
    private final AtomicInteger attemptCounter = new AtomicInteger(0);  // Counter to simulate initial failure

    public InitiatePaymentTasklet(PaymentApiClient paymentApiClient,
                                  RetryRegistry retryRegistry,
                                  CircuitBreakerRegistry circuitBreakerRegistry) {
        this.paymentApiClient = paymentApiClient;
        this.retry = retryRegistry.retry("initiatePaymentRetry");
        this.circuitBreaker = circuitBreakerRegistry.circuitBreaker("initiatePaymentCircuitBreaker");

        retry.getEventPublisher().onRetry(event ->
                logger.info("Retry attempt {} for initiatePayment", event.getNumberOfRetryAttempts()));

        circuitBreaker.getEventPublisher()
                .onSuccess(event -> logger.info("CircuitBreaker success on attempt {}", event.getElapsedDuration()))
                .onError(event -> logger.info("CircuitBreaker error: {}", event.getThrowable().getMessage()))
                .onStateTransition(event -> logger.info("CircuitBreaker state transition: from {} to {}",
                        event.getStateTransition().getFromState(), event.getStateTransition().getToState()));
    }

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        logger.info("InitiatePaymentTasklet - Starting payment initiation...");

        // Wrap the initiatePayment call with Retry and Circuit Breaker
        Supplier<String> initiatePaymentSupplier = CircuitBreaker.decorateSupplier(circuitBreaker,
                Retry.decorateSupplier(retry, this::simulateInitialFailure));

        // Retrieve transaction ID
        String transactionId = initiatePaymentSupplier.get();

        if (transactionId == null || transactionId.isEmpty()) {
            circuitBreaker.onError(0, java.util.concurrent.TimeUnit.SECONDS, new RuntimeException("Received empty transactionId."));
            throw new RuntimeException("Received empty transactionId.");
        }

        // Store transactionId in the job execution context
        ExecutionContext jobContext = chunkContext.getStepContext().getStepExecution().getJobExecution().getExecutionContext();
        jobContext.putString("transactionId", transactionId);

        logger.info("InitiatePaymentTasklet - Transaction ID retrieved: {}", transactionId);
        return RepeatStatus.FINISHED;
    }

    /**
     * Simulates an initial failure on the first attempt to trigger the retry mechanism.
     */
    private String simulateInitialFailure() {
        if (attemptCounter.getAndIncrement() == 0) {
            logger.info("Simulated failure on first attempt.");
            throw new RuntimeException("Simulated failure on first attempt");
        }
        // Proceed with actual API call after the first failure
        return paymentApiClient.initiatePayment();
    }
}
