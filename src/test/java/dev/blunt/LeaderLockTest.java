package dev.blunt;

import org.junit.jupiter.api.Test;

public class LeaderLockTest {

    @Test
    void leaderLockTest() {
        LeaderLock leader1 = new LeaderLock();
        leader1.tryAcquire();
    }
}
