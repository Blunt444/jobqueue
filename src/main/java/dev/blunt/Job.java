package dev.blunt;

public record Job(String id, String type, String payload, int attempts, int maxAttempts) {

    public Job(String id, String type, String payload) {
        this(id, type, payload, 0, 3);
    }
}
