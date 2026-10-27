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

            HandlerRegistry.clear();
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
    void testACR() {

        Job job = new Job("1", "email", "123");
        JobQueue.push(job);

        Job workerA = JobQueue.claim(300);

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
        assertEquals(1, readyCount());
        assertEquals(0, activeCount());

        Job workerB = JobQueue.claim(3000);

        assertNotNull(workerB);
        assertEquals(workerA.id(), workerB.id());
        assertTrue(JobQueue.ack(workerB));

        assertEquals(0, readyCount());
        assertEquals(0, activeCount());

    }

}
