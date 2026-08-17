package com.worktime.service;

import com.worktime.dto.task.CreateTaskRequest;
import com.worktime.dto.task.TaskResponse;
import com.worktime.dto.task.UpdateTaskStatusRequest;
import com.worktime.entity.Project;
import com.worktime.entity.ProjectStatus;
import com.worktime.entity.Task;
import com.worktime.entity.TaskPriority;
import com.worktime.entity.TaskStatus;
import com.worktime.entity.User;
import com.worktime.entity.UserRole;
import com.worktime.exception.ResourceNotFoundException;
import com.worktime.repository.ProjectRepository;
import com.worktime.repository.TaskRepository;
import com.worktime.repository.TaskStatusHistoryRepository;
import com.worktime.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private TaskStatusHistoryRepository
            taskStatusHistoryRepository;

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private TaskService taskService;

    @Test
    void createTaskShouldCreateTaskWithDefaultPriority() {
        // Arrange
        Project project = createActiveProject();

        User employee = createEmployee(
                2L,
                "employee@example.com"
        );

        CreateTaskRequest request =
                org.mockito.Mockito.mock(
                        CreateTaskRequest.class
                );

        LocalDate dueDate =
                LocalDate.of(2026, 8, 20);

        when(request.getProjectId()).thenReturn(1L);
        when(request.getTitle()).thenReturn("Write tests");
        when(request.getDescription())
                .thenReturn("Write service unit tests");
        when(request.getDueDate()).thenReturn(dueDate);
        when(request.getEstimatedDurationMinutes())
                .thenReturn(120);
        when(request.getPriority()).thenReturn(null);
        when(request.getAssignedUserId()).thenReturn(2L);

        when(projectRepository.findById(1L))
                .thenReturn(Optional.of(project));

        when(userRepository.findById(2L))
                .thenReturn(Optional.of(employee));

        when(taskRepository.save(any(Task.class)))
                .thenAnswer(invocation ->
                        invocation.getArgument(0)
                );

        // Act
        TaskResponse response =
                taskService.createTask(request);

        // Assert
        assertThat(response).isNotNull();

        ArgumentCaptor<Task> taskCaptor =
                ArgumentCaptor.forClass(Task.class);

        verify(taskRepository).save(
                taskCaptor.capture()
        );

        Task savedTask = taskCaptor.getValue();

        assertThat(savedTask.getTitle())
                .isEqualTo("Write tests");

        assertThat(savedTask.getDescription())
                .isEqualTo("Write service unit tests");

        assertThat(savedTask.getDueDate())
                .isEqualTo(dueDate);

        assertThat(
                savedTask.getEstimatedDurationMinutes()
        ).isEqualTo(120);

        assertThat(savedTask.getPriority())
                .isEqualTo(TaskPriority.MEDIUM);

        assertThat(savedTask.getStatus())
                .isEqualTo(TaskStatus.TODO);

        assertThat(savedTask.getProject())
                .isSameAs(project);

        assertThat(savedTask.getAssignedUser())
                .isSameAs(employee);
    }

    @Test
    void createTaskShouldThrowWhenProjectDoesNotExist() {
        // Arrange
        CreateTaskRequest request =
                org.mockito.Mockito.mock(
                        CreateTaskRequest.class
                );

        when(request.getProjectId()).thenReturn(99L);

        when(projectRepository.findById(99L))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() ->
                taskService.createTask(request)
        )
                .isInstanceOf(
                        ResourceNotFoundException.class
                )
                .hasMessage("Project not found!");

        verify(taskRepository, never())
                .save(any(Task.class));
    }

    @Test
    void createTaskShouldThrowWhenAssignedUserIsInactive() {
        // Arrange
        Project project = createActiveProject();

        User inactiveEmployee = createEmployee(
                2L,
                "employee@example.com"
        );
        inactiveEmployee.setActive(false);

        CreateTaskRequest request =
                org.mockito.Mockito.mock(
                        CreateTaskRequest.class
                );

        when(request.getProjectId()).thenReturn(1L);
        when(request.getDueDate())
                .thenReturn(
                        LocalDate.of(2026, 8, 20)
                );
        when(request.getAssignedUserId()).thenReturn(2L);

        when(projectRepository.findById(1L))
                .thenReturn(Optional.of(project));

        when(userRepository.findById(2L))
                .thenReturn(
                        Optional.of(inactiveEmployee)
                );

        // Act & Assert
        assertThatThrownBy(() ->
                taskService.createTask(request)
        )
                .isInstanceOf(
                        IllegalArgumentException.class
                )
                .hasMessage(
                        "Inactive users cannot be assigned to tasks"
                );

        verify(taskRepository, never())
                .save(any(Task.class));
    }

    @Test
    void getTaskByIdShouldThrowWhenEmployeeIsNotAssigned() {
        // Arrange
        Project project = createActiveProject();

        User assignedEmployee = createEmployee(
                2L,
                "assigned@example.com"
        );

        User currentEmployee = createEmployee(
                3L,
                "current@example.com"
        );

        Task task = createTask(
                project,
                assignedEmployee
        );

        when(taskRepository.findById(10L))
                .thenReturn(Optional.of(task));

        when(
                userRepository.findByEmailIgnoreCase(
                        "current@example.com"
                )
        ).thenReturn(Optional.of(currentEmployee));

        // Act & Assert
        assertThatThrownBy(() ->
                taskService.getTaskById(
                        10L,
                        "current@example.com"
                )
        )
                .isInstanceOf(
                        AccessDeniedException.class
                )
                .hasMessage(
                        "You do not have permission to access this task"
                );
    }

    @Test
    void updateTaskStatusShouldAllowAssignedEmployee() {
        // Arrange
        Project project = createActiveProject();

        User employee = createEmployee(
                2L,
                "employee@example.com"
        );

        Task task = createTask(project, employee);

        UpdateTaskStatusRequest request =
                org.mockito.Mockito.mock(
                        UpdateTaskStatusRequest.class
                );

        when(request.getStatus())
                .thenReturn(TaskStatus.COMPLETED);

        when(taskRepository.findById(10L))
                .thenReturn(Optional.of(task));

        when(
                userRepository.findByEmailIgnoreCase(
                        "employee@example.com"
                )
        ).thenReturn(Optional.of(employee));

        when(taskRepository.save(task))
                .thenReturn(task);

        // Act
        TaskResponse response =
                taskService.updateTaskStatus(
                        10L,
                        "employee@example.com",
                        request
                );

        // Assert
        assertThat(response).isNotNull();

        assertThat(task.getStatus())
                .isEqualTo(TaskStatus.COMPLETED);

        verify(taskRepository).save(task);
    }

    @Test
    void getOverdueTasksShouldReturnOnlyAssignedTasksForEmployee() {
        // Arrange
        User employee = createEmployee(
                2L,
                "employee@example.com"
        );

        Project project = createActiveProject();
        Task task = createTask(project, employee);

        LocalDate today = LocalDate.now();

        List<TaskStatus> openStatuses = List.of(
                TaskStatus.TODO,
                TaskStatus.IN_PROGRESS
        );

        when(
                userRepository.findByEmailIgnoreCase(
                        "employee@example.com"
                )
        ).thenReturn(Optional.of(employee));

        when(
                taskRepository
                        .findByAssignedUserAndDueDateBeforeAndStatusInOrderByDueDateAsc(
                                employee,
                                today,
                                openStatuses
                        )
        ).thenReturn(List.of(task));

        // Act
        List<TaskResponse> response =
                taskService.getOverdueTasks(
                        "employee@example.com"
                );

        // Assert
        assertThat(response).hasSize(1);

        verify(taskRepository)
                .findByAssignedUserAndDueDateBeforeAndStatusInOrderByDueDateAsc(
                        employee,
                        today,
                        openStatuses
                );
    }

    private Project createActiveProject() {
        Project project = Project.builder()
                .name("WorkTime")
                .description("Time management project")
                .startDate(
                        LocalDate.of(2026, 8, 1)
                )
                .endDate(
                        LocalDate.of(2026, 8, 31)
                )
                .status(ProjectStatus.ACTIVE)
                .build();

        project.setId(1L);

        return project;
    }

    private User createEmployee(
            Long id,
            String email
    ) {
        User user = new User();

        user.setId(id);
        user.setFirstName("Test");
        user.setLastName("Employee");
        user.setEmail(email);
        user.setRole(UserRole.EMPLOYEE);
        user.setActive(true);

        return user;
    }

    private Task createTask(
            Project project,
            User assignedUser
    ) {
        Task task = new Task();

        task.setId(10L);
        task.setTitle("Write tests");
        task.setDescription("Write service tests");
        task.setDueDate(
                LocalDate.of(2026, 8, 10)
        );
        task.setEstimatedDurationMinutes(120);
        task.setPriority(TaskPriority.MEDIUM);
        task.setStatus(TaskStatus.TODO);
        task.setProject(project);
        task.setAssignedUser(assignedUser);

        return task;
    }
}