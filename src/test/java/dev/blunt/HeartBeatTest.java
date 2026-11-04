package dev.blunt;

import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import redis.clients.jedis.Jedis;

public class HeartBeatTest {

    @BeforeEach
    void clear() {
        try (Jedis j = JobQueue.pool.getResource()) {
            j.del(JobQueue.readyKey);
            j.del(JobQueue.activeKey);
        }
    }

    private long readyCount() {
        try (Jedis j = JobQueue.pool.getResource()) {
            return j.llen(JobQueue.readyKey);
        }
    }

    private long activeCount() {
        try (Jedis j = JobQueue.pool.getResource()) {
            return j.zcard(JobQueue.activeKey);
        }
    }

    @Test
    void heartbeatTest() {
        Job job = new Job("1", "email", "123");
        JobQueue.push(job);

        Job workerA = JobQueue.claim(15_000);

        assertNotNull(workerA);
        assertEquals(0, readyCount());
        assertEquals(1, activeCount());

        try (var sheduler = Executors.newSingleThreadScheduledExecutor()) {
            sheduler.scheduleAtFixedRate(() -> {
                try {
                    JobQueue.renewLease(20_000, job);
                } catch (Exception e) {
                    System.out.println("Heartbeat test scheduler failed: " + e.getMessage());
                }
            }, 10, 10, TimeUnit.SECONDS);

            try {
                Thread.sleep(20_000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            assertEquals(0, JobQueue.requeueExpired());

            assertEquals(0, readyCount());
            assertEquals(1, activeCount());

            boolean result = JobQueue.ack(job);

            assertTrue(result);
        }

        assertEquals(0, activeCount());
    }
}
