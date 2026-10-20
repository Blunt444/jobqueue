package dev.blunt;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;

public class JobQueueTest {

    @Test
    void testPushPop() {
        Job original = new Job("1", "email", "hi");
        JobQueue.push(original);
        Job poll = JobQueue.poll();
        assertEquals(original, poll);
    }
}
