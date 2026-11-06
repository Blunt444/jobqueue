package dev.blunt;

import java.time.LocalDateTime;
import java.util.UUID;

public class Schedule {

    private final String id;
    private final CronExpression cronExpression;
    private final Job job;

    public Schedule(CronExpression cronExpression, Job job) {
        this.id = UUID.randomUUID().toString();
        this.cronExpression = cronExpression;
        this.job = job;
    }

    public boolean shouldRun(LocalDateTime dateTime) {
        return cronExpression.matches(dateTime);
    }

    public String getScheduleId() {
        return id;
    }

    public CronExpression getCronExpression() {
        return cronExpression;
    }

    public Job getJob() {
        return job;
    }
}
