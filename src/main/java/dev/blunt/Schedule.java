package dev.blunt;

import java.time.LocalDateTime;
import java.util.UUID;

public class Schedule {

    private final String id;
    private final CronExpression cronExpression;
    private final Job job;
    private final LocalDateTime createdAt;

    public Schedule(CronExpression cronExpression, Job job) {
        this.id = UUID.randomUUID().toString();
        this.cronExpression = cronExpression;
        this.job = job;
        this.createdAt = LocalDateTime.now();
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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
