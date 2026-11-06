package dev.blunt;

import org.junit.jupiter.api.Test;

public class CronExpressionParseTest {

    @Test
    void parseTest() {
        CronExpression result = CronExpression.parse("*/5 9-17 * * 1-5");
        System.out.println(result.toString());
    }
}
