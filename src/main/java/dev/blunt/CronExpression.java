package dev.blunt;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.TreeSet;

public class CronExpression {

    private Set<Integer> minutes;
    private Set<Integer> hours;
    private Set<Integer> daysOfMonth;
    private Set<Integer> months;
    private Set<Integer> daysOfWeek;

    public static Set<Integer> parseField(String field, int min, int max) {
        Set<Integer> result = new TreeSet<>();

        String[] splits = field.split(",", -1);

        for (String split : splits) {
            if (split.equals("*")) {
                for (int i = min; i <= max; i++) {
                    result.add(i);
                }
            } else if (split.contains("/")) {
                String[] pattern = split.split("/", -1);

                if (pattern.length <= 1 || pattern.length > 2) {
                    throw new IllegalArgumentException("Not a valid step expression");
                }

                try {
                    int step = Integer.parseInt(pattern[1]);

                    if (step <= 0) {
                        throw new IllegalArgumentException("Step value must be positive");
                    }

                    String[] range = pattern[0].split("-", -1);

                    if (range[0].equals("*")) {
                        for (int i = min; i <= max; i += step) {
                            result.add(i);
                        }
                    } else if (range.length == 1) {
                        try {
                            int startRange = Integer.parseInt(range[0]);

                            if (startRange < min || startRange > max) {
                                throw new IllegalArgumentException("Not a valid range value");
                            }

                            for (int i = startRange; i <= max; i += step) {
                                result.add(i);
                            }
                        } catch (NumberFormatException e) {
                            throw new IllegalArgumentException("Invalid range");
                        }
                    } else if (range.length > 2) {
                        throw new IllegalArgumentException("Multiple ranges");
                    } else if (range.length < 1) {
                        throw new IllegalArgumentException("Range is incomplete");
                    } else {
                        try {
                            int minRange = Integer.parseInt(range[0]);
                            int maxRange = Integer.parseInt(range[1]);

                            if (maxRange < minRange) {
                                throw new IllegalArgumentException("Not a valid range");
                            } else if (minRange < min || maxRange > max) {
                                throw new IllegalArgumentException("Not a valid range");
                            }

                            for (int i = minRange; i <= maxRange; i += step) {
                                result.add(i);
                            }

                        } catch (NumberFormatException e) {
                            throw new IllegalArgumentException("Not a valid range value");
                        }
                    }

                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException("Step value is invalid");
                }

            } else if (split.isEmpty()) {
                throw new IllegalArgumentException("Empty value found");
            } else if (split.contains("-")) {
                String[] range = split.split("-", -1);

                if (range.length > 2) {
                    throw new IllegalArgumentException("Multiple ranges");
                } else if (range.length <= 1) {
                    throw new IllegalArgumentException("Range is incomplete");
                }

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
                } else if (minRange < min || maxRange > max) {
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

                if (val > max || val < min) {
                    throw new IllegalArgumentException("Out of range");
                }

                result.add(val);
            }
        }

        return result;
    }

    public static CronExpression parse(String field) {
        String[] splits = field.split("\\s+");

        if (splits.length != 5) {
            throw new IllegalArgumentException("Invalid Expression");
        }

        CronExpression result = new CronExpression();

        result.minutes = parseField(splits[0], 0, 59);
        result.hours = parseField(splits[1], 0, 23);
        result.daysOfMonth = parseField(splits[2], 1, 31);
        result.months = parseField(splits[3], 1, 12);
        result.daysOfWeek = parseField(splits[4], 0, 7);

        return result;
    }

    public static boolean matches(LocalDateTime dateTime){

        return true;
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();

        result.append("[");
        for (int x : minutes) {
            result.append(x);
            result.append(" ");
        }
        result.append("]");
        result.append(" ");

        result.append("[");
        for (int x : hours) {
            result.append(x + " ");
        }
        result.append("]");
        result.append(" ");

        result.append("[");
        for (int x : daysOfMonth) {
            result.append(x + " ");
        }
        result.append("]");
        result.append(" ");

        result.append("[");
        for (int x : months) {
            result.append(x + " ");
        }
        result.append("]");
        result.append(" ");

        result.append("[");
        for (int x : daysOfWeek) {
            result.append(x + " ");
        }
        result.append("]");

        return result.toString();
    }
}
