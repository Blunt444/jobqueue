package dev.blunt;

import java.util.Set;

import org.junit.jupiter.api.Test;

public class CronExpressionParseTest {

    @Test
    void testfieldParse() {
        try {
            System.out.println(CronExpression.parseField("*", 0, 59));
            System.out.println(CronExpression.parseField("1,2,3", 0, 3));
            System.out.println(CronExpression.parseField("1-5", 0, 59));
            System.out.println(CronExpression.parseField("abx",0,59));
            System.out.println(CronExpression.parseField("1-0", 0, 29));
        } catch (IllegalArgumentException e) {
            System.out.println("Error Message: " + e.getMessage());
        }

    }
}
