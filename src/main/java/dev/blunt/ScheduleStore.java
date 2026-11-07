package dev.blunt;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;

import redis.clients.jedis.Jedis;

public class ScheduleStore {

    public final static String lastFiredKey = "scheduler:last-fired";
    public final static String scheduleFireScript = loadScript("lua/scheduleFire.lua");

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

    public static void fire(Schedule schedule, LocalDateTime dateTime, LeaderLock leaderLock) {
        try (Jedis j = JobQueue.pool.getResource()) {
            Object result = j.eval(
                    scheduleFireScript,
                    List.of(lastFiredKey, JobQueue.readyKey, LeaderLock.leaderKey),
                    List.of(schedule.getScheduleId(), dateTime.toString(), JobQueue.encode(schedule.getJob()), leaderLock.getOwnerId())
            );
        }
    }

    public static LocalDateTime getLastFired(Schedule schedule) {
        try (Jedis j = JobQueue.pool.getResource()) {
            String result = j.get(lastFiredKey + ":" + schedule.getScheduleId());

            if (result == null) {
                return null;
            }

            return LocalDateTime.parse(result);
        }
    }

}
