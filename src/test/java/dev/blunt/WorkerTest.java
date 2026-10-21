package dev.blunt;

import org.junit.jupiter.api.Test;

public class WorkerTest {
    @Test 
    void testWorker(){
        Worker w1 = new Worker(job -> System.out.println(job.id() + " " + job.type() + " " + job.payload()));
        Thread t1 = new Thread(w1::run);
        t1.start();

        JobQueue.push(new Job("1", "email", "Hi blunt"));
        JobQueue.push(new Job("2", "email", "Hi blunt! How are you"));
    }
}
