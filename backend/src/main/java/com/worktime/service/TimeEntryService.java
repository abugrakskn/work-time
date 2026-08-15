package com.worktime.service;

import com.worktime.dto.timeentry.CreateManualTimeEntryRequest;
import com.worktime.dto.timeentry.StartTimeEntryRequest;
import com.worktime.dto.timeentry.TimeEntryResponse;
import com.worktime.entity.Task;
import com.worktime.entity.TaskStatus;
import com.worktime.entity.TimeEntry;
import com.worktime.entity.User;
import com.worktime.exception.ResourceConflictException;
import com.worktime.exception.ResourceNotFoundException;
import com.worktime.repository.TimeEntryRepository;
import com.worktime.repository.TaskRepository;
import com.worktime.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TimeEntryService {

    private final TimeEntryRepository timeEntryRepository;
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;

    @Transactional
    public TimeEntryResponse startTimer(
            String email,
            StartTimeEntryRequest request
    ) {
        User user = getCurrentUser(email);

        if (timeEntryRepository.findByUserAndEndTimeIsNull(user).isPresent()) {
            throw new ResourceConflictException("User already has an active time entry.");
        }

        Task task = taskRepository.findById(request.getTaskId())
                .orElseThrow(() -> new ResourceNotFoundException("Task not found!"));

        if (!user.isAdmin() && !task.isAssignedTo(user)) {
            throw new AccessDeniedException("You do not have permission to track time for this task.");
        }

        if (task.getStatus() == TaskStatus.COMPLETED
                || task.getStatus() == TaskStatus.CANCELLED) {
            throw new IllegalArgumentException("Time tracking cannot be started for completed or cancelled tasks.");
        }

        TimeEntry timeEntry = TimeEntry.builder()
                .user(user)
                .task(task)
                .startTime(LocalDateTime.now())
                .description(request.getDescription())
                .build();

        TimeEntry savedTimeEntry = timeEntryRepository.save(timeEntry);

        return toResponse(savedTimeEntry);
    }

    @Transactional
    public TimeEntryResponse stopTimer(String email) {
        User user = getCurrentUser(email);

        TimeEntry activeTimeEntry = timeEntryRepository
                .findByUserAndEndTimeIsNull(user)
                .orElseThrow(() -> new ResourceConflictException("User does not have an active time entry."));

        LocalDateTime endTime = LocalDateTime.now();
        long durationMinutes = Duration.between(
                activeTimeEntry.getStartTime(),
                endTime
        ).toMinutes();
        int calculatedDurationMinutes = Math.toIntExact(durationMinutes);

        activeTimeEntry.setEndTime(endTime);
        activeTimeEntry.setDurationMinutes(calculatedDurationMinutes);
        TimeEntry savedTimeEntry = timeEntryRepository.save(activeTimeEntry);

        return toResponse(savedTimeEntry);
    }

    @Transactional
    public TimeEntryResponse createManualTimeEntry(
            String email,
            CreateManualTimeEntryRequest request
    ) {
        User user = getCurrentUser(email);
        Task task = taskRepository.findById(request.getTaskId())
                .orElseThrow(() -> new ResourceNotFoundException("Task not found!"));
        if (!user.isAdmin() && !task.isAssignedTo(user)) {
            throw new AccessDeniedException("You do not have permission to create time entry for this task.");
        }

        LocalDateTime startTime = request.getStartTime();
        LocalDateTime endTime = request.getEndTime();
        LocalDateTime now = LocalDateTime.now();
        if (!endTime.isAfter(startTime)) {
            throw new IllegalArgumentException("End time must be after start time.");
        }
        if (startTime.isAfter(now) || endTime.isAfter(now)) {
            throw new IllegalArgumentException("Manual time entries cannot contain future dates.");
        }

        long durationMinutes = Duration.between(startTime, endTime).toMinutes();
        if (durationMinutes < 1) {
            throw new IllegalArgumentException("Time entry duration must be at least one minute.");
        }
        int calculatedDurationMinutes = Math.toIntExact(durationMinutes);

        TimeEntry timeEntry = TimeEntry.builder()
                .user(user)
                .task(task)
                .startTime(startTime)
                .endTime(endTime)
                .durationMinutes(calculatedDurationMinutes)
                .description(request.getDescription())
                .build();
        TimeEntry savedTimeEntry = timeEntryRepository.save(timeEntry);
        return toResponse(savedTimeEntry);
    }

    @Transactional(readOnly = true)
    public Optional<TimeEntryResponse> getActiveTimeEntry(String email) {
        User user = getCurrentUser(email);
        return timeEntryRepository
                .findByUserAndEndTimeIsNull(user)
                .map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public List<TimeEntryResponse> getTimeEntries(String email) {
        User user = getCurrentUser(email);
        return timeEntryRepository
                .findByUserOrderByStartTimeDesc(user)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private TimeEntryResponse toResponse(TimeEntry timeEntry) {
        return new TimeEntryResponse(
                timeEntry.getId(),
                timeEntry.getUser().getId(),
                timeEntry.getUser().getFirstName()
                        + " "
                        + timeEntry.getUser().getLastName(),
                timeEntry.getTask().getId(),
                timeEntry.getTask().getTitle(),
                timeEntry.getStartTime(),
                timeEntry.getEndTime(),
                timeEntry.getDurationMinutes(),
                timeEntry.getDescription()
        );
    }

    private User getCurrentUser(String email) {
        return userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found!"));
    }

}
