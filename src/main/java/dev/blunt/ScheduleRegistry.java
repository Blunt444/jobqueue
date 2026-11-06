package dev.blunt;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class ScheduleRegistry {

    private final ConcurrentHashMap<String, Schedule> scheduleRegistry = new ConcurrentHashMap<>();

    public void register(Schedule schedule) {
        scheduleRegistry.put(schedule.getScheduleId(), schedule);
    }

    public boolean remove(String scheduleId) {
        Schedule result = scheduleRegistry.remove(scheduleId);

        if (result != null) {
            return true;
        }
        return false;
    }

    public Schedule getSchedule(String scheduleId) {
        return scheduleRegistry.get(scheduleId);
    }

    public Collection<Schedule> getAll() {
        return List.copyOf(scheduleRegistry.values());
    }
}
