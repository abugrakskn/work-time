package com.worktime.service;

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

import java.time.LocalDateTime;

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
        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found!"));

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

        return new TimeEntryResponse(
                savedTimeEntry.getId(),
                savedTimeEntry.getUser().getId(),
                savedTimeEntry.getUser().getFirstName()
                    + " "
                    + savedTimeEntry.getUser().getLastName(),
                savedTimeEntry.getTask().getId(),
                savedTimeEntry.getTask().getTitle(),
                savedTimeEntry.getStartTime(),
                savedTimeEntry.getEndTime(),
                savedTimeEntry.getDurationMinutes(),
                savedTimeEntry.getDescription()
        );
    }

}
