package dev.blunt;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import redis.clients.jedis.Jedis;

public class WorkerPoolTest {

    @BeforeEach
    public void clean() {
        try (Jedis j = JobQueue.pool.getResource()) {
            j.del("jobs");
        }
        HandlerRegistry.clear();
    }

    @Test
    public void testWorkerPool() {
        CountDownLatch done = new CountDownLatch(1000);
        Set<String> seen = ConcurrentHashMap.newKeySet();
        AtomicInteger calls = new AtomicInteger();
        WorkerPool pool = new WorkerPool(4);

        HandlerRegistry.register("email", job -> {
            System.out.println(job.id() + " " + job.type() + " " + job.payload());
            
            seen.add(job.id());
            calls.incrementAndGet();
            done.countDown();
        });

        pool.start();

        for (int i = 0; i < 1000; i++) {
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
        assertEquals(1000, seen.size());
        assertEquals(1000, calls.get());
    }
}
