package dev.blunt;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

public class ScheduleRunner {

    private final ScheduleRegistry scheduleRegistry;
    private final LeaderLock leaderLock;

    public ScheduleRunner(ScheduleRegistry scheduleRegistry, LeaderLock leaderLock) {
        this.scheduleRegistry = scheduleRegistry;
        this.leaderLock = leaderLock;
    }

    public void runOnce() {
        if (!leaderLock.isLeader()) {
            return;
        }

        LocalDateTime now = LocalDateTime.now();

        for (Schedule schedule : scheduleRegistry.getAll()) {
            if (schedule.shouldRun(now)) {
                catchUp(schedule, now);
            }
        }
    }

    private void catchUp(Schedule schedule, LocalDateTime now) {

        LocalDateTime lastFired = ScheduleStore.getLastFired(schedule);

        if (lastFired == null) {
            lastFired = schedule.getCreatedAt();
        }

        lastFired = lastFired.truncatedTo(ChronoUnit.MINUTES);
        LocalDateTime nowMinute = now.truncatedTo(ChronoUnit.MINUTES);

        long minutes = Math.min(1000, ChronoUnit.MINUTES.between(lastFired, nowMinute));

        for (int i = 1; i <= minutes; i++) {
            LocalDateTime candidate = lastFired.plusMinutes(i);

            if (schedule.shouldRun(candidate)) {
                ScheduleStore.fire(schedule, candidate, leaderLock);
            }
        }
    }
}
