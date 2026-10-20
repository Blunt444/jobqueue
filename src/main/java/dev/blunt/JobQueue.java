package dev.blunt;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

public class JobQueue {

    public static final JedisPool pool = new JedisPool("localhost", 6379);

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
            Job job = decode(j.rpop("jobs"));
            return job;
        }
    }
}
