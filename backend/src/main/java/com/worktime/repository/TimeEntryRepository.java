package com.worktime.repository;

import com.worktime.entity.Task;
import com.worktime.entity.TimeEntry;
import com.worktime.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TimeEntryRepository
        extends JpaRepository<TimeEntry, Long> {

    Optional<TimeEntry> findByUserAndEndTimeIsNull(
            User user
    );

    List<TimeEntry> findByUserOrderByStartTimeDesc(
            User user
    );

    List<TimeEntry> findByTaskOrderByStartTimeDesc(
            Task task
    );
}