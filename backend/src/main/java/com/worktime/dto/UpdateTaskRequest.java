package com.worktime.dto;

import com.worktime.entity.TaskPriority;
import com.worktime.entity.TaskStatus;
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

    @NotBlank(message = "Task title cannot be blank")
    @Size(max = 100, message = "Task title cannot exceed 100 characters")
    private String title;

    @Size(max = 1000, message = "Description cannot exceed 1000 characters")
    private String description;

    @FutureOrPresent(message = "Due date cannot be in the past")
    private LocalDate dueDate;

    @Positive(message = "Estimated duration must be greater than zero")
    private Integer estimatedDurationMinutes;

    private TaskPriority priority;
    private TaskStatus status;

    @NotNull(message = "Project id is required")
    private Long projectId;

    private Long assignedUserId;
}
