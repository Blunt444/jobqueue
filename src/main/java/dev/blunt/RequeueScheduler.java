package dev.blunt;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class RequeueScheduler {

    private static final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    public static void start() {

        scheduler.scheduleAtFixedRate(() -> {
            try {
                long count = JobQueue.requeueExpired();

                if (count > 0) {
                    System.out.println("Requeued: " + count + " jobs");
                }

            } catch (Exception e) {
                System.err.println("Failed to requeue jobs: " + e.getMessage());
            }
        }, 0, 1, TimeUnit.SECONDS);
    }

    public static void stop(){
        scheduler.shutdown();
    }
}
