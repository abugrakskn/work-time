package com.worktime.service;

import com.worktime.dto.timeentry.StartTimeEntryRequest;
import com.worktime.entity.Task;
import com.worktime.entity.TaskStatus;
import com.worktime.entity.TimeEntry;
import com.worktime.entity.User;
import com.worktime.entity.UserRole;
import com.worktime.exception.ResourceConflictException;
import com.worktime.repository.ProjectRepository;
import com.worktime.repository.TaskRepository;
import com.worktime.repository.TimeEntryRepository;
import com.worktime.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TimeEntryServiceTest {

    private static final ZoneId TEST_ZONE =
            ZoneId.of("Europe/Istanbul");

    private static final LocalDateTime FIXED_NOW =
            LocalDateTime.of(
                    2026,
                    8,
                    16,
                    12,
                    0
            );

    @Mock
    private TimeEntryRepository timeEntryRepository;

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ProjectRepository projectRepository;

    private TimeEntryService timeEntryService;

    @BeforeEach
    void setUp() {
        Clock fixedClock = Clock.fixed(
                Instant.parse("2026-08-16T09:00:00Z"),
                TEST_ZONE
        );

        timeEntryService = new TimeEntryService(
                timeEntryRepository,
                taskRepository,
                userRepository,
                projectRepository,
                fixedClock
        );
    }

    @Test
    void startTimerShouldRejectSecondActiveTimer() {
        // Arrange
        User user = createUser();
        TimeEntry activeTimeEntry = new TimeEntry();

        StartTimeEntryRequest request =
                org.mockito.Mockito.mock(
                        StartTimeEntryRequest.class
                );

        when(
                userRepository.findByEmailIgnoreCase(
                        "admin@example.com"
                )
        ).thenReturn(Optional.of(user));

        when(
                timeEntryRepository
                        .findByUserAndEndTimeIsNull(user)
        ).thenReturn(Optional.of(activeTimeEntry));

        // Act & Assert
        assertThatThrownBy(() ->
                timeEntryService.startTimer(
                        "admin@example.com",
                        request
                )
        )
                .isInstanceOf(
                        ResourceConflictException.class
                )
                .hasMessage(
                        "User already has an active time entry."
                );

        verify(taskRepository, never())
                .findById(anyLong());

        verify(timeEntryRepository, never())
                .save(any(TimeEntry.class));
    }

    @Test
    void startTimerShouldUseBackendClock() {
        // Arrange
        User user = createUser();
        Task task = createTask();

        StartTimeEntryRequest request =
                org.mockito.Mockito.mock(
                        StartTimeEntryRequest.class
                );

        when(request.getTaskId()).thenReturn(10L);
        when(request.getDescription())
                .thenReturn("Testing timer");

        when(
                userRepository.findByEmailIgnoreCase(
                        "admin@example.com"
                )
        ).thenReturn(Optional.of(user));

        when(
                timeEntryRepository
                        .findByUserAndEndTimeIsNull(user)
        ).thenReturn(Optional.empty());

        when(taskRepository.findById(10L))
                .thenReturn(Optional.of(task));

        when(
                timeEntryRepository.save(
                        any(TimeEntry.class)
                )
        ).thenAnswer(invocation ->
                invocation.getArgument(0)
        );

        // Act
        timeEntryService.startTimer(
                "admin@example.com",
                request
        );

        // Assert
        ArgumentCaptor<TimeEntry> captor =
                ArgumentCaptor.forClass(
                        TimeEntry.class
                );

        verify(timeEntryRepository).save(
                captor.capture()
        );

        TimeEntry savedTimeEntry =
                captor.getValue();

        assertThat(savedTimeEntry.getStartTime())
                .isEqualTo(FIXED_NOW);

        assertThat(savedTimeEntry.getEndTime())
                .isNull();

        assertThat(
                savedTimeEntry.getDurationMinutes()
        ).isNull();
    }

    @Test
    void stopTimerShouldCalculateDurationCorrectly() {
        // Arrange
        User user = createUser();
        Task task = createTask();

        TimeEntry activeTimeEntry =
                TimeEntry.builder()
                        .user(user)
                        .task(task)
                        .startTime(
                                FIXED_NOW.minusMinutes(95)
                        )
                        .description("Testing duration")
                        .build();

        when(
                userRepository.findByEmailIgnoreCase(
                        "admin@example.com"
                )
        ).thenReturn(Optional.of(user));

        when(
                timeEntryRepository
                        .findByUserAndEndTimeIsNull(user)
        ).thenReturn(Optional.of(activeTimeEntry));

        when(
                timeEntryRepository.save(
                        activeTimeEntry
                )
        ).thenReturn(activeTimeEntry);

        // Act
        timeEntryService.stopTimer(
                "admin@example.com"
        );

        // Assert
        assertThat(activeTimeEntry.getEndTime())
                .isEqualTo(FIXED_NOW);

        assertThat(
                activeTimeEntry.getDurationMinutes()
        ).isEqualTo(95);

        verify(timeEntryRepository)
                .save(activeTimeEntry);
    }

    @Test
    void stopTimerShouldThrowWhenNoActiveTimerExists() {
        // Arrange
        User user = createUser();

        when(
                userRepository.findByEmailIgnoreCase(
                        "admin@example.com"
                )
        ).thenReturn(Optional.of(user));

        when(
                timeEntryRepository
                        .findByUserAndEndTimeIsNull(user)
        ).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() ->
                timeEntryService.stopTimer(
                        "admin@example.com"
                )
        )
                .isInstanceOf(
                        ResourceConflictException.class
                )
                .hasMessage(
                        "User does not have an active time entry."
                );

        verify(timeEntryRepository, never())
                .save(any(TimeEntry.class));
    }

    private User createUser() {
        User user = new User();

        user.setId(1L);
        user.setFirstName("Ahmet");
        user.setLastName("Keskin");
        user.setEmail("admin@example.com");
        user.setRole(UserRole.ADMIN);
        user.setActive(true);

        return user;
    }

    private Task createTask() {
        Task task = new Task();

        task.setId(10L);
        task.setTitle("Test task");
        task.setStatus(TaskStatus.TODO);

        return task;
    }
}