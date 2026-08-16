package com.worktime.service;

import com.worktime.dto.timeentry.*;
import com.worktime.entity.*;
import com.worktime.exception.ResourceConflictException;
import com.worktime.exception.ResourceNotFoundException;
import com.worktime.repository.ProjectRepository;
import com.worktime.repository.TimeEntryRepository;
import com.worktime.repository.TaskRepository;
import com.worktime.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TimeEntryService {

    private final TimeEntryRepository timeEntryRepository;
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;
    private final Clock clock;

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
                .startTime(LocalDateTime.now(clock))
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

        LocalDateTime endTime = LocalDateTime.now(clock);
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
        LocalDateTime now = LocalDateTime.now(clock);
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

    @Transactional(readOnly = true)
    public TimeSummaryResponse getDailySummary(String email, LocalDate date) {
        User user = getCurrentUser(email);

        LocalDateTime startTime = date.atStartOfDay();
        LocalDateTime endTimeExclusive = date.plusDays(1).atStartOfDay();
        long totalDurationMinutes = timeEntryRepository
                .calculateTotalDurationMinutes(
                        user,
                        startTime,
                        endTimeExclusive
                );

        return new TimeSummaryResponse(date, date, totalDurationMinutes);
    }

    @Transactional(readOnly = true)
    public TimeSummaryResponse getWeeklySummary(String email, LocalDate date) {
        User user = getCurrentUser(email);

        LocalDate weekStart = date.with(
                TemporalAdjusters.previousOrSame(
                        DayOfWeek.MONDAY
                )
        );
        LocalDate weekEnd = weekStart.plusDays(6);
        LocalDateTime startTime = weekStart.atStartOfDay();
        LocalDateTime endTimeExclusive = weekEnd.plusDays(1).atStartOfDay();

        long totalDurationMinutes = timeEntryRepository
                .calculateTotalDurationMinutes(user, startTime, endTimeExclusive);
        return new TimeSummaryResponse(weekStart, weekEnd, totalDurationMinutes);
    }

    @Transactional(readOnly = true)
    public ProjectTimeReportResponse getProjectReport(
            Long projectId,
            LocalDate startDate,
            LocalDate endDate
    ) {
        validateDateRange(startDate, endDate);

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Project not found!"
                        )
                );

        LocalDateTime startTime =
                startDate.atStartOfDay();

        LocalDateTime endTimeExclusive =
                endDate.plusDays(1).atStartOfDay();

        List<TimeEntryResponse> timeEntries =
                timeEntryRepository
                        .findCompletedByProjectAndDateRange(
                                project,
                                startTime,
                                endTimeExclusive
                        )
                        .stream()
                        .map(this::toResponse)
                        .toList();

        long totalDurationMinutes = timeEntries.stream()
                .map(TimeEntryResponse::durationMinutes)
                .mapToLong(Integer::longValue)
                .sum();

        return new ProjectTimeReportResponse(
                project.getId(),
                project.getName(),
                startDate,
                endDate,
                totalDurationMinutes,
                timeEntries
        );
    }

    @Transactional(readOnly = true)
    public UserTimeReportResponse getUserReport(
            Long userId,
            LocalDate startDate,
            LocalDate endDate
    ) {
        validateDateRange(startDate, endDate);

        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User not found!"
                        )
                );

        LocalDateTime startTime =
                startDate.atStartOfDay();

        LocalDateTime endTimeExclusive =
                endDate.plusDays(1).atStartOfDay();

        List<TimeEntryResponse> timeEntries =
                timeEntryRepository
                        .findCompletedByUserAndDateRange(
                                user,
                                startTime,
                                endTimeExclusive
                        )
                        .stream()
                        .map(this::toResponse)
                        .toList();

        long totalDurationMinutes = timeEntries.stream()
                .map(TimeEntryResponse::durationMinutes)
                .mapToLong(Integer::longValue)
                .sum();

        String userName =
                user.getFirstName()
                        + " "
                        + user.getLastName();

        return new UserTimeReportResponse(
                user.getId(),
                userName,
                startDate,
                endDate,
                totalDurationMinutes,
                timeEntries
        );
    }

    private void validateDateRange(
            LocalDate startDate,
            LocalDate endDate
    ) {
        if (endDate.isBefore(startDate)) {
            throw new IllegalArgumentException(
                    "End date must be on or after start date."
            );
        }
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
