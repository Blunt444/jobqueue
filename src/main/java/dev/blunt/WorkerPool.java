package dev.blunt;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class WorkerPool {

    private final int poolSize;
    private final ExecutorService executor;
    private final List<Worker> workers = new ArrayList<>();

    WorkerPool(int poolSize) {

        if (poolSize <= 0) {
            throw new IllegalArgumentException("Pool size must be greater than 0.");
        }

        this.poolSize = poolSize;
        executor = Executors.newFixedThreadPool(poolSize);
        createWorkers();
    }

    private void createWorkers() {
        for (int i = 0; i < poolSize; i++) {
            Worker w = new Worker();
            workers.add(w);
        }
    }

    public void start() {
        for (int i = 0; i < poolSize; i++) {
            executor.submit(workers.get(i)::run);
        }
    }

    public void shutdown() {

        for(Worker worker : workers){
            worker.stop();
        }

        executor.shutdown();

        try {
            boolean finished = executor.awaitTermination(5, TimeUnit.SECONDS);

            if(!finished){
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }

        executor.shutdown();
    }
}
