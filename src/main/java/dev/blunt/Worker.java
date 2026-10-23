package dev.blunt;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public class Worker {

    public final Map<String, Consumer<Job>> handlers = new ConcurrentHashMap<>();
    private volatile boolean running = true;

    // Worker(Consumer<Job> handler) {
    //     this.handler = handler;
    // }
    void run() {
        while (running) {
            Job job = JobQueue.poll();

            if (job == null) {
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    System.out.println("A worker just died");
                    Thread.currentThread().interrupt();
                    running = false;
                }
            } else {
                Consumer<Job> handler = handlers.get(job.type());
                if (handler != null) {
                    try {
                        handler.accept(job);
                    } catch (Exception e) {
                        System.out.println("job id: " + job.id() + " failed: " + e.getMessage());
                    }
                } else {
                    System.out.println("no handler for this type " + job.type() + " job id: " + job.id());
                }
            }
        }
    }

    public void stop() {
        running = false;
    }
}
