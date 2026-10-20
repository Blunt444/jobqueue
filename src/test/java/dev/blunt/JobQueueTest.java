package dev.blunt;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import org.junit.jupiter.api.Test;

public class JobQueueTest {

    @Test
    void testPushPop() {
        Job a = new Job("1", "email", "hi");
        Job b = new Job("2", "email", "hi how");

        JobQueue.push(a);
        JobQueue.push(b);

        Job poll1 = JobQueue.poll();
        Job poll2 = JobQueue.poll();
        Job poll3 = JobQueue.poll();

        assertNotNull(poll1);
        assertNotNull(poll2);

        assertNull(poll3);

        assertEquals(a, poll1);
        assertEquals(b, poll2);
    }
}
