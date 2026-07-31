package com.worktime.dto;

import com.worktime.entity.TaskPriority;
import com.worktime.entity.TaskStatus;
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
public class UpdateTaskRequest {

    @Schema(description = "Updated title of the task",
            example = "Implement JWT authentication")
    @NotBlank(message = "Task title cannot be blank")
    @Size(max = 100, message = "Task title cannot exceed 100 characters")
    private String title;

    @Schema(description = "Updated description of the task",
            example = "Implement JWT authentication and configure Spring Security")
    @Size(max = 1000, message = "Description cannot exceed 1000 characters")
    private String description;

    @Schema(description = "Updated due date of the task",
            example = "2027-08-01")
    @FutureOrPresent(message = "Due date cannot be in the past")
    private LocalDate dueDate;

    @Schema(description = "Updated estimated duration of the task in minutes",
            example = "100")
    @Positive(message = "Estimated duration must be greater than zero")
    private Integer estimatedDurationMinutes;

    @Schema(description = "Updated priority level of the task (optional)",
            example = "HIGH")
    private TaskPriority priority;

    @Schema(description = "Current status of the task",
            example = "DONE")
    private TaskStatus status;

    @Schema(description = "Updated project ID that the task belongs to",
            example = "1")
    @NotNull(message = "Project id is required")
    private Long projectId;

    @Schema(description = "Updated ID of the user assigned to the task (optional)",
            example = "1")
    private Long assignedUserId;
}
