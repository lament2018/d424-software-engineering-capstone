package com.schedule.service;

import com.schedule.model.Schedule;
import com.schedule.repository.ScheduleRepository;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class ScheduleService {
    private final ScheduleRepository scheduleRepository;

    public ScheduleService(ScheduleRepository scheduleRepository) {
        this.scheduleRepository = scheduleRepository;
    }
    public List<Schedule> findAll() {
        return scheduleRepository.findAll();
    }
    public Schedule save(Schedule schedule) {
        return scheduleRepository.save(schedule);
    }
}
