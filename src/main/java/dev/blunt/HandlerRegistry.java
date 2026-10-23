package dev.blunt;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public class HandlerRegistry {

    private final static Map<String, Consumer<Job>> handlers = new ConcurrentHashMap<>();

    public static void register(String type, Consumer<Job> handler) {
        handlers.put(type, handler);
    }

    public static Consumer<Job> getHandler(String type) {
        return handlers.get(type);
    }

    public static void clear() {
        handlers.clear();
    }
}
