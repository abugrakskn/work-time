package com.worktime.repository;

import com.worktime.entity.Task;
import com.worktime.entity.TimeEntry;
import com.worktime.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
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

    @Query("""
        SELECT COALESCE(SUM(timeEntry.durationMinutes), 0)
        FROM TimeEntry timeEntry
        WHERE timeEntry.user = :user
          AND timeEntry.startTime >= :startTime
          AND timeEntry.startTime < :endTimeExclusive
          AND timeEntry.durationMinutes IS NOT NULL
        """)
    Long calculateTotalDurationMinutes(
            @Param("user") User user,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTimeExclusive")
            LocalDateTime endTimeExclusive
    );
}