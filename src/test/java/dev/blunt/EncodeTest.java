package dev.blunt;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;

public class EncodeTest {

    @Test
    void testEncodeDecode() {
        Job original = new Job("1", "email", "hi");
        Job decode = JobQueue.decode(JobQueue.encode(original));
        assertEquals(original, decode);
    }

    @Test
    void testEncodeDecodeWithTab() {
        Job original = new Job("2", "email", "hi\thow are you");
        Job decode = JobQueue.decode(JobQueue.encode(original));
        assertEquals(original, decode);
    }
}
