package dev.blunt;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import redis.clients.jedis.Jedis;

public class LeaderLockTest {

    @BeforeEach
    void clear() {
        try (Jedis j = JobQueue.pool.getResource()) {
            j.del(LeaderLock.leaderKey);
        }
    }

    @Test
    void leaderLockTest() throws InterruptedException {
        LeaderLock leader1 = new LeaderLock();
        assertTrue(leader1.tryAcquire());

        Thread.sleep(12_000);

        LeaderLock leader2 = new LeaderLock();
        assertTrue(leader2.tryAcquire());
        assertFalse(leader1.tryAcquire());

        Thread.sleep(12_000);

        assertFalse(leader2.renew());

        assertTrue(leader2.tryAcquire());
        assertFalse(leader1.release());
        assertTrue(leader2.release());
        assertFalse(leader2.release());
    }
}
