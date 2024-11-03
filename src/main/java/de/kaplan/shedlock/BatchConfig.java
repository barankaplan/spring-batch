package de.kaplan.shedlock;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
public class BatchConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;

    public BatchConfig(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        this.jobRepository = jobRepository;
        this.transactionManager = transactionManager;
    }

    @Bean
    public Job paymentJob(Step initiatePaymentStep, Step verifyPaymentStep, Step completePaymentStep) {
        return new JobBuilder("paymentJob", jobRepository)
                .start(initiatePaymentStep)
                .next(verifyPaymentStep)
                .next(completePaymentStep)
                .build();
    }

    @Bean
    public Step initiatePaymentStep(InitiatePaymentTasklet tasklet) {
        return new StepBuilder("initiatePaymentStep", jobRepository)
                .tasklet(tasklet, transactionManager)
                .allowStartIfComplete(true)
                .build();
    }

    @Bean
    public Step verifyPaymentStep(VerifyPaymentTasklet tasklet) {
        return new StepBuilder("verifyPaymentStep", jobRepository)
                .tasklet(tasklet, transactionManager)
                .allowStartIfComplete(true)
                .build();
    }

    @Bean
    public Step completePaymentStep(CompletePaymentTasklet tasklet) {
        return new StepBuilder("completePaymentStep", jobRepository)
                .tasklet(tasklet, transactionManager)
                .allowStartIfComplete(true)
                .build();
    }
}
