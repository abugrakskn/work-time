package com.worktime.dto;

import com.worktime.entity.TaskPriority;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreateTaskRequest {

    @Schema(description = "Title of the task",
            example = "Implement JWT authentication")
    @NotBlank(message = "Task title cannot be blank!")
    @Size(max = 100, message = "Title cannot exceed 100 characters!")
    private String title;

    @Schema(description = "Detailed description of the task",
            example = "Implement JWT authentication and configure Spring Security")
    @Size(max = 1000, message = "Description cannot exceed 1000 characters!")
    private String description;

    @Schema(description = "Due date of the task",
            example = "2026-08-01")
    @FutureOrPresent(message = "Due date cannot be in the past!")
    private LocalDate dueDate;

    @Schema(description = "Estimated duration of the task in minutes",
            example = "120")
    @Positive(message = "Estimated duration must be greater than zero!")
    private Integer estimatedDurationMinutes;

    @Schema(description = "Priority level of the task (optional). Defaults to MEDIUM if not provided.",
            example = "HIGH")
    private TaskPriority priority;

    @Schema(description = "ID of the project that the task belongs to",
            example = "1")
    @NotNull(message = "Project ID is required!")
    private Long projectId;

    @Schema(description = "ID of the user assigned to the task (optional)",
            example = "2")
    private Long assignedUserId;
}
