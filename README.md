
# Payment Workflow Documentation

This documentation provides a detailed explanation of the payment workflow created using Spring Batch and Resilience4j. The workflow handles external API calls with resilience mechanisms using `Retry` and `CircuitBreaker` for robust error management.

---

## General Workflow

The payment workflow consists of three main steps:
1. **Initiate Payment (initiatePaymentStep)**
2. **Verify Payment (verifyPaymentStep)**
3. **Complete Payment (completePaymentStep)**

These steps are executed sequentially, and each is wrapped with Resilience4j’s `Retry` and `CircuitBreaker` mechanisms to manage errors.

---

## Key Components of the Workflow

### 1. PaymentApiClient

`PaymentApiClient` simulates interactions with an external API during the payment workflow. The methods called during the workflow are:
- **initiatePayment**: Initiates the payment and returns a transaction ID.
- **verifyPaymentStatus**: Verifies the payment status and returns a random result for demonstration.
- **completePayment**: Completes the payment.

Each method simulates an external API call and follows specific error scenarios to test the resilience mechanisms.

### 2. Spring Batch Workflow Steps

Each step operates as a `Tasklet` and carries data through `ExecutionContext`. The steps work as follows:

1. **initiatePaymentStep**:
   - Simulates a failure on the first attempt to trigger the retry mechanism.
   - Retrieves a `transactionId` and saves it to the `ExecutionContext`.

2. **verifyPaymentStep**:
   - Verifies the payment status by calling the external API. If the status check fails, the `Retry` mechanism activates. If the failure rate reaches a certain threshold, the `CircuitBreaker` interrupts the process.
   - Marks the step as `FINISHED` if verification succeeds.

3. **completePaymentStep**:
   - Completes the payment and ends the workflow successfully if all conditions are met.

### 3. Error Handling with Resilience4j

The workflow employs `Retry` and `CircuitBreaker` mechanisms for error handling:

- **Retry**: Retries failed attempts a specified number of times to overcome temporary issues.
- **CircuitBreaker**: Stops operations if the failure rate reaches 50%, transitioning to an `Open` state. When `wait-duration-in-open-state` expires (e.g., 5 seconds), the CircuitBreaker moves to a `Half-Open` state to test the service again.

#### CircuitBreaker Calculation

The CircuitBreaker measures the failure rate over the last 10 calls. If the failure rate exceeds 50%, it switches to `Open` (open) mode and stops new calls for the `wait-duration-in-open-state` period. After this time, the CircuitBreaker moves to `Half-Open` mode and re-evaluates. If the next call succeeds, the CircuitBreaker returns to `Closed` mode; otherwise, it reopens.

### 4. Data Management with ExecutionContext

Spring Batch uses `ExecutionContext` to share data between steps:
- **transactionId**: Obtained in `initiatePaymentStep` and saved to `ExecutionContext` for subsequent use.

---

## Workflow Scenario

The workflow includes specific failure simulations and resilience mechanisms for each step:

1. **initiatePaymentStep**:
   - The first call is simulated to fail, triggering the retry mechanism.
   - Retrieves the `transactionId` and saves it to `ExecutionContext` once successful.

2. **verifyPaymentStep**:
   - Checks the payment status. If it fails, the retry mechanism activates. If the failure persists, the CircuitBreaker interrupts the process.
   - If successful, the step is marked as `FINISHED`.

3. **completePaymentStep**:
   - Completes the payment, ending the workflow if all prior steps were successful.

## Independent Workflows in a foreach Loop

In a `foreach` loop, each customer’s workflow can run as an independent job instance:
- **Independent Transactions**: Each job operates independently, so a failure in one job does not affect the others.
- **Parallel Execution**: Each customer’s workflow can execute in parallel, allowing high concurrency.

```java
for (Customer customer : customers) {
    JobParameters jobParameters = new JobParametersBuilder()
            .addString("customerId", customer.getId().toString())
            .addDate("runDate", new Date())
            .toJobParameters();

    jobLauncher.run(paymentJob, jobParameters);
}
```

---

## Error and Transaction Management

Error and transaction handling are managed as follows:

- **Retry Mechanism**: Manages transient errors, retrying failed attempts and ensuring process continuity.
- **CircuitBreaker**: Stops the process if the error rate exceeds 50%. After a specified time, it re-evaluates the service.
- **Transaction Management**: Each step operates independently, so a failure in one step does not affect the others.

### Step-by-Step Workflow

1. **First Step (initiatePaymentStep)**:
   - Starts the payment initiation, simulating a failure on the first attempt. The retry mechanism attempts the call again.
   - Retrieves the `transactionId` and saves it to `ExecutionContext` once successful.

2. **Second Step (verifyPaymentStep)**:
   - Attempts to verify payment status. If it fails, retry attempts are made. After repeated failures, the CircuitBreaker stops further attempts.
   - If successful, the step is marked as `FINISHED`.

3. **Third Step (completePaymentStep)**:
   - Completes the payment and marks the workflow as successfully completed.

---

## foreach Loop Note on Independent Workflow Execution

Using a `foreach` loop, each job instance runs independently:
- **No Infinite Loops**: Each job completes independently, with each customer’s workflow ending without impacting others.
- **Independent Transactions**: Each job instance handles a separate customer transaction, allowing independent error handling.

---

## Summary: Spring Batch and Resilience4j Advantages

The combination of Spring Batch and Resilience4j provides several advantages:
- **High Availability**: Processes are resilient to transient errors, allowing workflows to complete successfully.
- **Advanced Error Management**: Retry and CircuitBreaker mechanisms offer flexibility and resilience.
- **Independent Transaction Management**: Each job instance operates independently, ensuring that a failure in one job does not affect others.

This documentation covers the complete structure of the payment workflow, its error-handling mechanisms, and data management strategies.
