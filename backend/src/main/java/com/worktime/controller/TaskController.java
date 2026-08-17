package com.worktime.controller;

import com.worktime.dto.task.*;
import com.worktime.service.TaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Task Management",
    description = "Operations for managing tasks.")
@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @GetMapping
    @Operation(summary = "Get all tasks",
                description = "Returns tasks accessible to the authenticated user")
    @ApiResponse(responseCode = "200",
            description = "Tasks retrieved successfully")
    public List<TaskResponse> getAllTasks(Authentication authentication){
        return taskService.getAllTasks(authentication.getName());
    }

    @GetMapping("/overdue")
    @Operation(summary = "Get overdue tasks",
            description = "Returns open tasks with a due date before today that are accessible to the authenticated user.")
    @ApiResponses({
            @ApiResponse(responseCode = "200",
                    description = "Overdue tasks retrieved successfully"),
            @ApiResponse(responseCode = "401",
                    description = "Authentication required"),
            @ApiResponse(responseCode = "404",
                    description = "User not found")
    })
    public List<TaskResponse> getOverdueTasks(
            Authentication authentication
    ) {
        return taskService.getOverdueTasks(
                authentication.getName()
        );
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get task by ID",
                description = "Returns the task with the specified ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200",
                        description = "Task retrieved successfully"),
            @ApiResponse(responseCode = "404",
                        description = "Task not found")
    })
    public TaskResponse getTaskById(@PathVariable Long id, Authentication authentication){
        return taskService.getTaskById(id, authentication.getName());
    }

    @GetMapping("/{id}/status-history")
    @Operation(summary = "Get task status history",
            description = "Returns status changes for a task ordered from newest to oldest.")
    @ApiResponses({
            @ApiResponse(responseCode = "200",
                    description = "Task status history retrieved successfully"),
            @ApiResponse(responseCode = "401",
                    description = "Authentication required"),
            @ApiResponse(responseCode = "403",
                    description = "User does not have permission to access this task history"),
            @ApiResponse(responseCode = "404",
                    description = "Task not found")
    })
    public List<TaskStatusHistoryResponse>
    getTaskStatusHistory(@PathVariable Long id, Authentication authentication) {
        return taskService.getTaskStatusHistory(
                id,
                authentication.getName()
        );
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a new task",
            description = "Creates a new task using the provided task details")
    @ApiResponses({
            @ApiResponse(responseCode = "201",
                        description = "Task created successfully"),
            @ApiResponse(responseCode = "400",
                        description = "Invalid task data")
    })
    public TaskResponse createTask(@Valid @RequestBody CreateTaskRequest request){
        return taskService.createTask(request);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a task",
            description = "Updates the task with the specified ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200",
                    description = "Task updated successfully"),
            @ApiResponse(responseCode = "400",
                    description = "Invalid task data"),
            @ApiResponse(responseCode = "404",
                    description = "Task not found")
    })
    public TaskResponse updateTask(
            @PathVariable Long id,
            Authentication authentication,
            @Valid @RequestBody UpdateTaskRequest request
    ) {
        return taskService.updateTask(
                id,
                authentication.getName(),
                request
        );
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Update task status",
            description = "Update the status of the task with specified ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200",
                    description = "Task status updated successfully"),
            @ApiResponse(responseCode = "400",
                    description = "Invalid task status"),
            @ApiResponse(responseCode = "401",
                    description = "Authentication required"),
            @ApiResponse(responseCode = "403",
                    description = "User does not have permission to update this task"),
            @ApiResponse(responseCode = "404",
                    description = "Task not found")
    })
    public TaskResponse updateTaskStatus(
            @PathVariable Long id,
            Authentication authentication,
            @Valid @RequestBody UpdateTaskStatusRequest request
    ) {
        return taskService.updateTaskStatus(id, authentication.getName(), request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete a task",
            description = "Deletes the task with the specified ID")
    @ApiResponses({
            @ApiResponse(responseCode = "204",
                    description = "Task deleted successfully"),
            @ApiResponse(responseCode = "404",
                    description = "Task not found")
    })
    public void deleteTask(@PathVariable Long id){
        taskService.deleteTask(id);
    }

}
