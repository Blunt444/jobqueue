package dev.blunt;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import redis.clients.jedis.Jedis;

public class FencingTest {

    @BeforeEach
    void clear() {
        try (Jedis j = JobQueue.pool.getResource()) {
            j.del(JobQueue.readyKey, JobQueue.activeKey, JobQueue.retryKey);
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

    private long retryCount() {
        try (Jedis j = JobQueue.pool.getResource()) {
            return j.zcard(JobQueue.retryKey);
        }
    }

    @Test
    void fencingTestWithTwoWorkers() throws InterruptedException {
        Job job = new Job("1", "email", "123");
        JobQueue.push(job);

        ClaimedJob workerA = JobQueue.claim(300);

        assertNotNull(workerA);
        assertEquals(0, readyCount());
        assertEquals(1, activeCount());

        Thread.sleep(500);

        assertEquals(1, JobQueue.requeueExpired());

        Thread.sleep(1500);

        assertEquals(1, JobQueue.retryJobs());

        ClaimedJob workerB = JobQueue.claim(500);

        assertNotNull(workerB);
        assertTrue(workerB.fencingToken() > workerA.fencingToken());

        assertFalse(JobQueue.ack(workerA.job(), workerA.fencingToken()));
        assertTrue(JobQueue.ack(workerB.job(), workerB.fencingToken()));

        assertEquals(0, activeCount());
        assertEquals(0, retryCount());
    }
}
