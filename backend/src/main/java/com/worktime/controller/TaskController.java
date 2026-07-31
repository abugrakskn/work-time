package com.worktime.controller;

import com.worktime.dto.CreateTaskRequest;
import com.worktime.dto.TaskResponse;
import com.worktime.dto.UpdateTaskRequest;
import com.worktime.service.TaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Task Management",
    description = "Operations for managing tasks.")
@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService){
        this.taskService = taskService;
    }

    @GetMapping
    @Operation(summary = "Get all tasks",
                description = "Returns all tasks in the system")
    @ApiResponse(responseCode = "200",
            description = "Tasks retrieved successfully")
    public List<TaskResponse> getAllTasks(){
        return taskService.getAllTasks();
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
    public TaskResponse getTaskById(@PathVariable Long id){
        return taskService.getTaskById(id);
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
            @Valid @RequestBody UpdateTaskRequest request
    ) {
        return taskService.updateTask(id, request);
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
