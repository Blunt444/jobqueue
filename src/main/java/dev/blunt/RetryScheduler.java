package dev.blunt;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class RetryScheduler {

    private static final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    public static void start() {
        scheduler.scheduleAtFixedRate(() -> {
            long count = JobQueue.retryJobs();

            if (count > 0) {
                System.out.println("retried " + count + " jobs");
            }
        }, 0, 1, TimeUnit.SECONDS);
    }

    public static void stop() {
        scheduler.shutdown();
    }
}
