package dev.blunt;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import redis.clients.jedis.Jedis;

public class DelayedjobsTest {

    @BeforeEach
    void clear() {
        try (Jedis j = JobQueue.pool.getResource()) {
            j.del(JobQueue.readyKey, JobQueue.delayKey);
        }
    }

    private long readyCount() {
        try (Jedis j = JobQueue.pool.getResource()) {
            return j.llen(JobQueue.readyKey);
        }
    }

    private long delayedCount() {
        try (Jedis j = JobQueue.pool.getResource()) {
            return j.zcard(JobQueue.delayKey);
        }
    }

    @Test
    void validateDelayedjobs() throws InterruptedException {
        Job job = new Job("1", "email", "123");
        JobQueue.push(job, 2_000);

        assertEquals(0, readyCount());
        assertEquals(1, delayedCount());

        Thread.sleep(3_000);

        assertEquals(1, JobQueue.promoteDelayedJobs());
        assertEquals(1, readyCount());
        assertEquals(0, delayedCount());

        ClaimedJob claimedJob = JobQueue.claim(30_000);

        assertNotNull(claimedJob);
        assertEquals(job.id(), claimedJob.job().id());
        assertTrue(JobQueue.ack(claimedJob.job(), claimedJob.fencingToken()));

    }
}
