package dev.blunt;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

public class RedisSmokeTest {

    @Test
    void canReachRedis() {
        try (JedisPool pool = new JedisPool("localhost", 6379); Jedis j = pool.getResource()) {
            assertEquals("PONG", j.ping());
        }
    }
}
