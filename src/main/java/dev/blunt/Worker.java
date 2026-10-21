package dev.blunt;

import java.util.function.Consumer;

public class Worker {

    private final Consumer<Job> handler;
    private volatile boolean running = true;

    Worker(Consumer<Job> handler) {
        this.handler = handler;
    }

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
            }
            else{
                handler.accept(job);
            }
        }
    }

    public void stop() {
        running = false;
    }
}
