package dev.blunt;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

public class CronExpressionMatchesTest {

    @Test
    void testMatches() {
        CronExpression parse1 = CronExpression.parse("* * * 1-12 1-4");

        System.out.println(parse1.matches(LocalDateTime.now()));
    }
}
