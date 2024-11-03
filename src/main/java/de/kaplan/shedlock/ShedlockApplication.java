package de.kaplan.shedlock;

import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;


@SpringBootApplication
@EnableScheduling
public class ShedlockApplication {

    public static void main(String[] args) {
        SpringApplication.run(ShedlockApplication.class, args);
    }

    @Bean
    public ApplicationRunner jobRunner(JobLauncher jobLauncher, Job paymentJob) {
        return args -> {
            JobParameters jobParameters = new JobParametersBuilder()
                    .addLong("startAt", System.currentTimeMillis())
                    .toJobParameters();

            jobLauncher.run(paymentJob, jobParameters);
        };
    }

}
