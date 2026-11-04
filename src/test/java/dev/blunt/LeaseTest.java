package dev.blunt;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import redis.clients.jedis.Jedis;

public class LeaseTest {

    @BeforeEach
    void clear() {
        try (Jedis j = JobQueue.pool.getResource()) {

            j.del(JobQueue.readyKey);
            j.del(JobQueue.activeKey);
            j.del(JobQueue.attemptsKey);
            j.del(JobQueue.retryKey);
            j.del(JobQueue.deadKey);

            HandlerRegistry.clear();
        }
    }

    private long readyCount() {
        try (Jedis j = JobQueue.pool.getResource()) {
            return j.llen(JobQueue.readyKey);
        }
    }

    private long attemptsCount() {
        try (Jedis j = JobQueue.pool.getResource()) {
            return j.zcard(JobQueue.attemptsKey);
        }
    }

    private long retryCount() {
        try (Jedis j = JobQueue.pool.getResource()) {
            return j.zcard(JobQueue.retryKey);
        }
    }

    private long deadCount() {
        try (Jedis j = JobQueue.pool.getResource()) {
            return j.llen(JobQueue.deadKey);
        }
    }

    private long activeCount() {
        try (Jedis j = JobQueue.pool.getResource()) {
            return j.zcard(JobQueue.activeKey);
        }
    }

    @Test
    void testACR() {

        Job job = new Job("1", "email", "123");
        JobQueue.push(job);

        ClaimedJob workerA = JobQueue.claim(300);

        assertNotNull(workerA);
        assertEquals(0, readyCount());
        assertEquals(1, activeCount());

        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        long count = JobQueue.requeueExpired();

        assertEquals(1, count);
        assertEquals(1, retryCount());
        assertEquals(0, activeCount());
        assertEquals(0, readyCount());

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        count = JobQueue.retryJobs();

        assertEquals(1, count);
        assertEquals(0, retryCount());
        assertEquals(1, readyCount());

        ClaimedJob workerB = JobQueue.claim(3000);

        assertNotNull(workerB);
        assertEquals(workerA.job().id(), workerB.job().id());
        assertTrue(JobQueue.ack(workerB.job(), workerB.fencingToken()));

        assertEquals(0, readyCount());
        assertEquals(0, activeCount());

    }

    @Test
    void testJobDead() {
        Job job = new Job("1", "email", "123", 0, 1);
        JobQueue.push(job);

        ClaimedJob workerA = JobQueue.claim(300);

        assertNotNull(workerA);
        assertEquals(0, readyCount());
        assertEquals(1, activeCount());

        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        long count = JobQueue.requeueExpired();

        assertEquals(1, count);
        assertEquals(0, activeCount());
        assertEquals(0, readyCount());
        assertEquals(0, retryCount());
        assertEquals(1, deadCount());

    }

}
