package com.worktime.service;

import com.worktime.dto.CreateTaskRequest;
import com.worktime.dto.TaskResponse;
import com.worktime.dto.UpdateTaskRequest;
import com.worktime.entity.Project;
import com.worktime.entity.Task;
import com.worktime.entity.TaskStatus;
import com.worktime.entity.User;
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
                .orElseThrow(() -> new RuntimeException("Project not Found!"));

        Task task = new Task();
        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());
        task.setDueDate(request.getDueDate());
        task.setStatus(TaskStatus.TODO);

        task.setProject(project);

        if (request.getAssignedUserId() != null) {
            User user = userRepository.findById(request.getAssignedUserId())
                    .orElseThrow(() -> new RuntimeException("User not Found!"));
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
                .orElseThrow(() -> new RuntimeException("Task not Found!"));

        return toResponse(task);
    }

    public TaskResponse updateTask(Long id, UpdateTaskRequest request){
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Task not Found!"));

        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());
        task.setDueDate(request.getDueDate());
        task.setStatus(request.getStatus());

        Project project = projectRepository.findById(request.getProjectId())
                .orElseThrow(() -> new RuntimeException("Project not found!"));
        task.setProject(project);

        if (request.getAssignedUserId() != null) {
            User user = userRepository.findById(request.getAssignedUserId())
                    .orElseThrow(() -> new RuntimeException("User not found!"));
            task.setAssignedUser(user);
        }

        Task updatedTask = taskRepository.save(task);

        return toResponse(updatedTask);
    }

    public void deleteTask(Long id){
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Task not Found!"));

        taskRepository.delete(task);
    }



    private TaskResponse toResponse(Task task){
        Project project = task.getProject();
        User user = task.getAssignedUser();

        return new TaskResponse(
                task.getId(),
                task.getTitle(),
                task.getDescription(),
                task.getDueDate(),
                task.getStatus(),
                project.getId(),
                project.getName(),
                user.getId(),
                user.getFirstName()
        );
    }
}
