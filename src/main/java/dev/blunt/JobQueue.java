package dev.blunt;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

public class JobQueue {

    public static final JedisPool pool = new JedisPool("localhost", 6379);

    public static final String readyKey = "jobs";
    public static final String activeKey = "jobs:active";
    public static final String retryKey = "jobs:retry";
    public static final String deadKey = "jobs:dead";
    public static final String attemptsKey = "jobs:attempts";

    private static final String claimScript = loadScript("lua/claim.lua");
    private static final String requeueScript = loadScript("lua/requeue.lua");
    private static final String ackScript = loadScript("lua/ack.lua");
    private static final String retryScript = loadScript("lua/retry.lua");
    private static final String renewLeaseScript = loadScript("lua/renewLease.lua");

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
        String encodedString = job.id() + "\t" + job.type() + "\t" + job.maxAttempts() + "\t" + job.payload();
        return encodedString;
    }

    public static Job decode(String encodedString) {
        String[] decodedString = encodedString.split("\t", 4);
        Job job = new Job(decodedString[0], decodedString[1], decodedString[3], 0, Integer.parseInt(decodedString[2]));
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
                    java.util.List.of(readyKey, activeKey, attemptsKey),
                    java.util.List.of(String.valueOf(leaseMs))
            );

            if (result == null) {
                return null;
            }

            return decode((String) result);

        }
    }

    public static long requeueExpired() {
        int batchSize = 100;
        int cap = 50000;
        int base = 1000;
        try (Jedis j = pool.getResource()) {

            Object result = j.eval(
                    requeueScript,
                    java.util.List.of(retryKey, activeKey, attemptsKey, deadKey),
                    java.util.List.of(String.valueOf(batchSize), String.valueOf(cap), String.valueOf(base))
            );

            return ((Number) result).longValue();
        }
    }

    public static long retryJobs() {
        int batchSize = 100;
        try (Jedis j = pool.getResource()) {
            Object result = j.eval(
                    retryScript,
                    java.util.List.of(readyKey, retryKey),
                    java.util.List.of(String.valueOf(batchSize))
            );

            return ((Number) result).longValue();
        }
    }

    public static boolean ack(Job job) {
        try (Jedis j = pool.getResource()) {
            String jobEncode = encode(job);

            Object result = j.eval(
                    ackScript,
                    java.util.List.of(activeKey),
                    java.util.List.of(jobEncode)
            );

            return ((Number) result).longValue() == 1;
        }
    }

    public static boolean renewLease(long ms, Job job) {
        try (Jedis j = pool.getResource()) {
            Object result = j.eval(
                    renewLeaseScript,
                    java.util.List.of(activeKey),
                    java.util.List.of(String.valueOf(ms), encode(job))
            );

            return ((Number) result).longValue() == 1;
        }
    }
}
