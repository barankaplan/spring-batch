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
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.stereotype.Service;

import java.util.function.Supplier;

@Service
public class CompletePaymentTasklet implements Tasklet {

    private static final Logger logger = LoggerFactory.getLogger(CompletePaymentTasklet.class);

    private final PaymentApiClient paymentApiClient;
    private final Retry retry;
    private final CircuitBreaker circuitBreaker;

    public CompletePaymentTasklet(PaymentApiClient paymentApiClient,
                                  RetryRegistry retryRegistry,
                                  CircuitBreakerRegistry circuitBreakerRegistry) {
        this.paymentApiClient = paymentApiClient;
        this.retry = retryRegistry.retry("completePaymentRetry");
        this.circuitBreaker = circuitBreakerRegistry.circuitBreaker("completePaymentCircuitBreaker");

        retry.getEventPublisher().onRetry(event ->
                logger.info("Retry attempt {} for completePayment", event.getNumberOfRetryAttempts()));

        circuitBreaker.getEventPublisher()
                .onSuccess(event -> logger.info("CircuitBreaker success on attempt {}", event.getElapsedDuration()))
                .onError(event -> logger.warn("CircuitBreaker error: {}", event.getThrowable().getMessage()))
                .onStateTransition(event -> logger.info("CircuitBreaker state transition: from {} to {}",
                        event.getStateTransition().getFromState(), event.getStateTransition().getToState()));
    }

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        logger.info("CompletePaymentTasklet - Starting payment completion...");

        String transactionId = (String) chunkContext.getStepContext().getJobExecutionContext().get("transactionId");

        Supplier<Void> completePaymentSupplier = CircuitBreaker.decorateSupplier(circuitBreaker,
                Retry.decorateSupplier(retry, () -> {
                    paymentApiClient.completePayment(transactionId);
                    return null;
                }));

        completePaymentSupplier.get();

        logger.info("CompletePaymentTasklet - Payment successfully completed.");
        return RepeatStatus.FINISHED;
    }
}
