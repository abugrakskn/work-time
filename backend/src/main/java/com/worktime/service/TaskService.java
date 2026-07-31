package com.worktime.service;

import com.worktime.dto.CreateTaskRequest;
import com.worktime.dto.TaskResponse;
import com.worktime.dto.UpdateTaskRequest;
import com.worktime.entity.*;
import com.worktime.exception.ResourceNotFoundException;
import com.worktime.repository.ProjectRepository;
import com.worktime.repository.TaskRepository;
import com.worktime.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;

    public TaskResponse createTask(CreateTaskRequest request){
        Project project = projectRepository
                .findById(request.getProjectId())
                .orElseThrow(() -> new ResourceNotFoundException("Project not found!"));

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

        if (request.getAssignedUserId() != null) {
            User user = userRepository.findById(request.getAssignedUserId())
                    .orElseThrow(() -> new ResourceNotFoundException("User not found!"));
            task.setAssignedUser(user);
        }

        Task createdTask = taskRepository.save(task);

        return toResponse(createdTask);
    }

    public List<TaskResponse> getAllTasks(){
        return taskRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public TaskResponse getTaskById(Long id){
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found!"));

        return toResponse(task);
    }

    public TaskResponse updateTask(Long id, UpdateTaskRequest request){
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found!"));

        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());
        task.setEstimatedDurationMinutes(request.getEstimatedDurationMinutes());
        if (request.getPriority() != null) {
            task.setPriority(request.getPriority());
        }
        task.setDueDate(request.getDueDate());
        task.setStatus(request.getStatus());

        Project project = projectRepository.findById(request.getProjectId())
                .orElseThrow(() -> new ResourceNotFoundException("Project not found!"));
        task.setProject(project);

        if (request.getAssignedUserId() == null) {
            task.setAssignedUser(null);
        } else {
            User user = userRepository.findById(request.getAssignedUserId())
                    .orElseThrow(() ->
                            new ResourceNotFoundException("User not found")
                    );

            task.setAssignedUser(user);
        }

        Task updatedTask = taskRepository.save(task);

        return toResponse(updatedTask);
    }

    public void deleteTask(Long id){
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found!"));

        taskRepository.delete(task);
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
