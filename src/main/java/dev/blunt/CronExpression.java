package dev.blunt;

import java.util.HashSet;
import java.util.Set;

public class CronExpression {

    public static Set<Integer> parseField(String field, int min, int max) {
        Set<Integer> result = new HashSet<>();

        String[] splits = field.split(",");

        for (String split : splits) {
            if (field.contains("*")) {
                for (int i = min; i <= max; i++) {
                    result.add(i);
                }
            } else if (split.contains("-")) {
                String[] range = split.split("-");

                int minRange = 0;
                int maxRange = 0;

                try {
                    minRange = Integer.parseInt(range[0]);
                    maxRange = Integer.parseInt(range[range.length - 1]);
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException("Not a Number");
                }

                if (maxRange < minRange) {
                    throw new IllegalArgumentException("Range shouldn't be reversed");
                } else if (maxRange > max) {
                    throw new IllegalArgumentException("Out of range");
                }

                for (int i = minRange; i <= maxRange; i++) {
                    result.add(i);
                }
            } else {
                int val = 0;
                try {
                    val = Integer.parseInt(split);
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException("Not a Number");
                }

                if(val > max || val < min){
                    throw new IllegalArgumentException("Out of range");
                }

                result.add(val);
            }
        }

        return result;
    }
}
