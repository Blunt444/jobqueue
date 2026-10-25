package dev.blunt;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class WorkerPoolTest {

    @BeforeEach
    public void clean() {
        HandlerRegistry.clear();
    }

    @Test
    public void testWorkerPool() {
        CountDownLatch done = new CountDownLatch(100);
        WorkerPool pool = new WorkerPool(3);

        HandlerRegistry.register("email", job -> {
            System.out.println(job.id() + " " + job.type() + " " + job.payload());
            done.countDown();
        });

        pool.start();

        for (int i = 0; i < 100; i++) {
            JobQueue.push(new Job(String.valueOf(i), "email", "This is a email for " + i));
        }

        boolean finished = false;

        try {
            finished = done.await(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            pool.shutdown();
        }

        assertTrue(finished, "Worker did not finish all jobs");
        assertEquals(done.getCount(), 0);
    }
}
