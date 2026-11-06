package dev.blunt;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

import redis.clients.jedis.Jedis;

public class LeaderLock {

    private static String leaderKey = "scheduler:leader";
    private final String ownerId;
    private final String renewScript = loadScript("lua/renewLeader.lua");

    public LeaderLock() {
        this.ownerId = UUID.randomUUID().toString();
    }

    private static String loadScript(String resource) {
        try (InputStream in = JobQueue.class.getClassLoader().getResourceAsStream(resource)) {
            if (in == null) {
                throw new IllegalStateException("lua script not found");
            }

            return new String(in.readAllBytes(), StandardCharsets.UTF_8);

        } catch (IOException e) {
            throw new IllegalStateException("Couldn't laod lua scripts");
        }
    }

    public boolean tryAcquire() {

        try (Jedis j = JobQueue.pool.getResource()) {
            String result = j.set(
                    leaderKey,
                    ownerId,
                    redis.clients.jedis.params.SetParams.setParams().nx().px(10_000)
            );

            return "OK".equals(result);
        }
    }

    public boolean renew() {
        try (Jedis j = JobQueue.pool.getResource()) {
            Object result = j.eval(
                    renewScript,
                    List.of(leaderKey),
                    List.of(ownerId, String.valueOf(10000))
            );

            return ((Number) result).longValue() == 1;
        }
    }
}
