package dev.blunt;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import redis.clients.jedis.Jedis;

public class AckCrashTest {

    // @BeforeEach
    // void clear() {
    //     HandlerRegistry.clear();

    //     try (Jedis j = JobQueue.pool.getResource()) {
    //         j.del(JobQueue.readyKey);
    //         j.del(JobQueue.activeKey);
    //     }
    // }

    // private long activeCount() {
    //     try (Jedis j = JobQueue.pool.getResource()) {
    //         return j.zcard(JobQueue.activeKey);
    //     }
    // }

    // private long readyCount() {
    //     try (Jedis j = JobQueue.pool.getResource()) {
    //         return j.llen(JobQueue.readyKey);
    //     }
    // }

    // @Test
    // void ackCrashTest() throws Exception {

    //     Job job = new Job("1", "email", "123");
    //     JobQueue.push(job);

    //     HandlerRegistry.register("email", jobArg -> {
    //         System.out.println("Worker is processing " + jobArg.id());
    //     });

    //     Worker worker = new Worker();
    //     Thread t1 = new Thread(worker::run);
    //     t1.start();

    //     RequeueScheduler.start();

    //     Thread.sleep(40_000);

    //     assertEquals(1, readyCount());
    //     assertEquals(0, activeCount());

    //     worker.stop();
    //     t1.interrupt();
    //     t1.join(2_000);

    //     RequeueScheduler.stop();
    // }

    // it is hard to similuate a one time crash for me to test this reliably.
}
