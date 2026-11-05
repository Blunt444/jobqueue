package dev.blunt;

import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;

public class CronExpressionParseTest {

    @Test
    void testfieldParse() {

        System.out.println(CronExpression.parseField("*", 0, 59));
        System.out.println(CronExpression.parseField("1,2,3", 0, 3));
        System.out.println(CronExpression.parseField("1-5", 0, 59));
        System.out.println(CronExpression.parseField("2", 0, 59));
        System.out.println(CronExpression.parseField("1,3-5", 0, 59));
        System.out.println(CronExpression.parseField("3,1,5", 0, 59));
        System.out.println(CronExpression.parseField("1/5", 0, 59));

        assertThrows(IllegalArgumentException.class, () -> CronExpression.parseField("abx", 0, 59));
        assertThrows(IllegalArgumentException.class, () -> CronExpression.parseField("1-0", 0, 29));
        assertThrows(IllegalArgumentException.class, () -> CronExpression.parseField("", 0, 59));
        assertThrows(IllegalArgumentException.class, () -> CronExpression.parseField("1,,3", 0, 59));
        assertThrows(IllegalArgumentException.class, () -> CronExpression.parseField("-5", 0, 59));
        assertThrows(IllegalArgumentException.class, () -> CronExpression.parseField("5-/5", 0, 59));
    }
}
