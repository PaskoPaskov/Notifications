public class RetryExecutor {

    // Execute an action with retry logic and exponential backoff
    public  static void execute(
            Runnable action,
            int attempts,
            long initialDelayMillis,
            String module,
            String operationName,
            LogErrors logErrors
    ) {

        long delay = initialDelayMillis;

        for (int i = 1; i <= attempts; i++) {
            try {
                action.run();
                return; // Success
            } catch (Exception e) {

                logErrors.log(
                        module,
                        "ERROR",
                        operationName + " failed (" + i + "/" + attempts + ")",
                        e
                );

                if (i == attempts) {
                    break;
                }

                try {
                    Thread.sleep(delay);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    logErrors.log("RetryExecutor", "WARN", "Retry interrupted", ie);
                    return;
                }

                delay *= 2; // exponential backoff
            }
        }

        // All retry attempts failed
        logErrors.log(
                module,
                "ERROR",
                operationName + " permanently failed after retries",
                null
        );
    }
}

