package dev.blunt;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import redis.clients.jedis.Jedis;

public class WorkerTest {

    @BeforeEach
    void clean() {
        try (Jedis j = JobQueue.pool.getResource()) {
            j.del("jobs");
        }
        HandlerRegistry.clear();
    }

    @Test
    void testWorker() {
        CountDownLatch done = new CountDownLatch(3);
        Worker w1 = new Worker();
        HandlerRegistry.register("email", job -> {
            System.out.println(job.id() + " " + job.type() + " " + job.payload());
            done.countDown();
        });
        HandlerRegistry.register("report", job -> {
            System.out.println(job.type() + " " + job.payload());
            done.countDown();
        });

        Thread t1 = new Thread(w1::run);
        t1.start();

        JobQueue.push(new Job("1", "email", "Hi blunt"));
        JobQueue.push(new Job("2", "email", "Hi blunt! How are you"));
        JobQueue.push(new Job("3", "report", "Hi blunt! your report"));

        boolean finished = false;

        try {
            finished = done.await(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        w1.stop();
        assertTrue(finished, "Worker did not finish all jobs");

        try {
            t1.join(2000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @Test
    void testWorkerWith100Jobs() {
        List<String> seen = Collections.synchronizedList(new ArrayList<>());
        CountDownLatch done = new CountDownLatch(100);

        Worker w = new Worker();

        HandlerRegistry.register("email", job -> {
            seen.add(job.id());
            done.countDown();
        });

        Thread t = new Thread(w::run);
        t.start();

        for (int i = 0; i < 100; i++) {
            JobQueue.push(new Job(String.valueOf(i), "email", "p" + i));
        }

        boolean fininshed = false;

        try {
            fininshed = done.await(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        w.stop();
        try {
            t.join(2000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        assertTrue(fininshed, "Worker did not process all jobs");
        assertEquals(100, seen.size());

        for (int i = 0; i < seen.size(); i++) {
            assertEquals(String.valueOf(i), seen.get(i));
        }
    }
}
