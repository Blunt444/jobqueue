package dev.blunt;

// import java.util.Map;
// import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public class Worker {

    // public final Map<String, Consumer<Job>> handlers = new ConcurrentHashMap<>();
    private volatile boolean running = true;

    // Worker(Consumer<Job> handler) {
    //     this.handler = handler;
    // }
    void run() {
        while (running) {
            Job job = JobQueue.claim(30_000);

            if (job == null) {
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    System.out.println("A worker just died");
                    Thread.currentThread().interrupt();
                    running = false;
                }
            } else {
                Consumer<Job> handler = HandlerRegistry.getHandler(job.type());
                if (handler != null) {
                    try {
                        handler.accept(job);

                        if (!JobQueue.ack(job)) {
                            System.out.println("Couldn't ack the job " + job.id());
                        }
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
