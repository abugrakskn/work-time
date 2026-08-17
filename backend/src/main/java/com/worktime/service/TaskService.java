package com.worktime.service;

import com.worktime.dto.task.*;
import com.worktime.entity.*;
import com.worktime.exception.ResourceNotFoundException;
import com.worktime.repository.ProjectRepository;
import com.worktime.repository.TaskRepository;
import com.worktime.repository.TaskStatusHistoryRepository;
import com.worktime.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final TaskStatusHistoryRepository taskStatusHistoryRepository;

    private User getCurrentUser(String email){
        return userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found!"));
    }

    public TaskResponse createTask(CreateTaskRequest request){
        Project project = projectRepository
                .findById(request.getProjectId())
                .orElseThrow(() -> new ResourceNotFoundException("Project not found!"));

        validateProjectCanAcceptNewTask(project);

        validateTaskDueDate(
                project,
                request.getDueDate()
        );

        TaskPriority priority = request.getPriority() != null
                ? request.getPriority()
                : TaskPriority.MEDIUM;

        Task task = new Task();
        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());
        task.setDueDate(request.getDueDate());
        task.setEstimatedDurationMinutes(request.getEstimatedDurationMinutes());
        task.setPriority(priority);
        task.setStatus(TaskStatus.TODO);

        task.setProject(project);

        task.setAssignedUser(
                resolveAssignedUser(request.getAssignedUserId())
        );

        Task createdTask = taskRepository.save(task);

        return toResponse(createdTask);
    }

    public List<TaskResponse> getAllTasks(String email){
        User currentUser = getCurrentUser(email);
        List<Task> tasks;

        if (currentUser.isAdmin()){
            tasks = taskRepository.findAll();
        }else {
            tasks = taskRepository.findByAssignedUser(currentUser);
        }

        return tasks.stream()
                .map(this::toResponse)
                .toList();
    }

    public List<TaskResponse> getOverdueTasks(
            String email
    ) {
        User currentUser = getCurrentUser(email);

        LocalDate today = LocalDate.now();

        List<TaskStatus> openStatuses = List.of(
                TaskStatus.TODO,
                TaskStatus.IN_PROGRESS
        );

        List<Task> overdueTasks;

        if (currentUser.isAdmin()) {
            overdueTasks =
                    taskRepository
                            .findByDueDateBeforeAndStatusInOrderByDueDateAsc(
                                    today,
                                    openStatuses
                            );
        } else {
            overdueTasks =
                    taskRepository
                            .findByAssignedUserAndDueDateBeforeAndStatusInOrderByDueDateAsc(
                                    currentUser,
                                    today,
                                    openStatuses
                            );
        }

        return overdueTasks.stream()
                .map(this::toResponse)
                .toList();
    }

    public TaskResponse getTaskById(Long id, String email){
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found!"));
        User currentUser = getCurrentUser(email);

        if (currentUser.isAdmin()){
            return toResponse(task);
        }
        if (task.isAssignedTo(currentUser)) {
            return toResponse(task);
        }
        throw new AccessDeniedException("You do not have permission to access this task");
    }

    @Transactional(readOnly = true)
    public List<TaskStatusHistoryResponse>
    getTaskStatusHistory(
            Long taskId,
            String email
    ) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Task not found!"
                        )
                );

        User currentUser = getCurrentUser(email);

        if (
                !currentUser.isAdmin()
                        && !task.isAssignedTo(currentUser)
        ) {
            throw new AccessDeniedException(
                    "You do not have permission to access this task history."
            );
        }

        return taskStatusHistoryRepository
                .findByTaskOrderByChangedAtDesc(task)
                .stream()
                .map(this::toHistoryResponse)
                .toList();
    }

    @Transactional
    public TaskResponse updateTask(
            Long id,
            String email,
            UpdateTaskRequest request
    ) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Task not found!"
                        )
                );

        User currentUser = getCurrentUser(email);

        TaskStatus previousStatus = task.getStatus();

        validateDueDateForUpdate(
                task,
                request.getDueDate()
        );

        Project project = projectRepository
                .findById(request.getProjectId())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Project not found!"
                        )
                );

        boolean projectChanged =
                !task.getProject()
                        .getId()
                        .equals(project.getId());

        if (projectChanged) {
            validateProjectCanAcceptNewTask(project);
        }

        validateTaskDueDate(
                project,
                request.getDueDate()
        );

        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());
        task.setEstimatedDurationMinutes(
                request.getEstimatedDurationMinutes()
        );
        task.setPriority(request.getPriority());
        task.setDueDate(request.getDueDate());
        task.setStatus(request.getStatus());
        task.setProject(project);

        task.setAssignedUser(
                resolveAssignedUser(
                        request.getAssignedUserId()
                )
        );

        Task updatedTask = taskRepository.save(task);

        recordStatusChange(
                updatedTask,
                previousStatus,
                request.getStatus(),
                currentUser
        );

        return toResponse(updatedTask);
    }

    @Transactional
    public TaskResponse updateTaskStatus(Long id, String email, UpdateTaskStatusRequest request){
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found!"));
        User currentUser = getCurrentUser(email);

        if (!currentUser.isAdmin() && !task.isAssignedTo(currentUser)) {
            throw new AccessDeniedException(
                    "You do not have permission to update this task."
            );
        }

        TaskStatus previousStatus = task.getStatus();
        TaskStatus newStatus = request.getStatus();
        if (previousStatus == newStatus) {
            return toResponse(task);
        }
        task.setStatus(newStatus);
        Task updatedTask = taskRepository.save(task);
        recordStatusChange(
                updatedTask,
                previousStatus,
                newStatus,
                currentUser
        );
        return toResponse(updatedTask);
    }

    public void deleteTask(Long id){
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found!"));

        taskRepository.delete(task);
    }

    private void validateDueDateForUpdate(
            Task task,
            LocalDate requestedDueDate
    ) {
        if (requestedDueDate == null) {
            return;
        }

        if (!requestedDueDate.isBefore(LocalDate.now())) {
            return;
        }

        if (requestedDueDate.equals(task.getDueDate())) {
            return;
        }

        throw new IllegalArgumentException("Due date cannot be changed to a past date");
    }

    private void validateProjectCanAcceptNewTask(
            Project project
    ) {
        if (!project.canAcceptNewTasks()) {
            throw new IllegalArgumentException(
                    "Completed or cancelled projects cannot accept new tasks"
            );
        }
    }

    private void validateTaskDueDate(Project project, LocalDate dueDate){
        if (dueDate == null){
            return;
        }
        if (dueDate.isBefore(project.getStartDate())) {
            throw new IllegalArgumentException("Task due date cannot be before the project start date");
        }
        if (dueDate.isAfter(project.getEndDate())) {
            throw new IllegalArgumentException("Task due date cannot be after the project end date");
        }

    }

    private User resolveAssignedUser(Long userId) {
        if (userId == null) {
            return null;
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found!"));

        if (!user.isActive()) {
            throw new IllegalArgumentException("Inactive users cannot be assigned to tasks");
        }

        return user;
    }

    private TaskStatusHistoryResponse toHistoryResponse(
            TaskStatusHistory history
    ) {
        User changedBy = history.getChangedBy();

        String changedByUserName =
                changedBy.getFirstName()
                        + " "
                        + changedBy.getLastName();

        return new TaskStatusHistoryResponse(
                history.getId(),
                history.getPreviousStatus(),
                history.getNewStatus(),
                changedBy.getId(),
                changedByUserName,
                history.getChangedAt()
        );
    }

    private void recordStatusChange(
            Task task,
            TaskStatus previousStatus,
            TaskStatus newStatus,
            User changedBy
    ) {
        if (previousStatus == newStatus) {
            return;
        }

        TaskStatusHistory history =
                TaskStatusHistory.builder()
                        .task(task)
                        .previousStatus(previousStatus)
                        .newStatus(newStatus)
                        .changedBy(changedBy)
                        .changedAt(LocalDateTime.now())
                        .build();

        taskStatusHistoryRepository.save(history);
    }

    private TaskResponse toResponse(Task task){
        Project project = task.getProject();
        User assignedUser = task.getAssignedUser();

        Long assignedUserId = assignedUser != null
                ? assignedUser.getId()
                : null;

        String assignedUserName = assignedUser != null
                ? assignedUser.getFirstName() + " " + assignedUser.getLastName()
                : null;

        return new TaskResponse(
                task.getId(),
                task.getTitle(),
                task.getDescription(),
                task.getDueDate(),
                task.getEstimatedDurationMinutes(),
                task.getPriority(),
                task.getStatus(),
                project.getId(),
                project.getName(),
                assignedUserId,
                assignedUserName
        );
    }
}
