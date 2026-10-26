package dev.blunt;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

public class JobQueue {

    public static final JedisPool pool = new JedisPool("localhost", 6379);

    private static final String readyKey = "jobs";
    private static final String activeKey = "jobs:active";

    private static final String claimScript = loadScript("lua/claim.lua");
    private static final String requeueScript = loadScript("lua/requeue.lua");

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

    public static String encode(Job job) {
        String encodedString = job.id() + "\t" + job.type() + "\t" + job.payload();
        return encodedString;
    }

    public static Job decode(String encodedString) {
        String[] decodedString = encodedString.split("\t", 3);
        Job job = new Job(decodedString[0], decodedString[1], decodedString[2]);
        return job;
    }

    public static boolean push(Job job) {
        try (Jedis j = pool.getResource()) {
            j.lpush("jobs", encode(job));
            return true;
        }
    }

    public static Job poll() {
        try (Jedis j = pool.getResource()) {
            String encodedString = j.rpop("jobs");

            if (encodedString == null) {
                return null;
            }

            return decode(encodedString);
        }
    }

    public static Job claim(long leaseMs) {
        try (Jedis j = pool.getResource()) {

            Object result = j.eval(
                    claimScript,
                    java.util.List.of(readyKey, activeKey),
                    java.util.List.of(String.valueOf(leaseMs))
            );

            if (result == null) {
                return null;
            }

            return decode((String) result);

        }
    }

    public static long requeueExpired(){
        int batchSize = 100;
        try(Jedis j = pool.getResource()) {
            
            Object result = j.eval(
                requeueScript,
                java.util.List.of(readyKey,activeKey),
                java.util.List.of(String.valueOf(batchSize))
            );

            return ((Number) result).longValue();
        }
    }
}
