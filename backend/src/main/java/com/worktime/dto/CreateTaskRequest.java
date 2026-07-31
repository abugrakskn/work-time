package com.worktime.dto;

import com.worktime.entity.TaskPriority;
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

    @NotBlank(message = "Task title cannot be blank!")
    @Size(max = 100, message = "Title cannot exceed 100 characters!")
    private String title;

    @Size(max = 1000, message = "Description cannot exceed 1000 characters!")
    private String description;

    @FutureOrPresent(message = "Due date cannot be in the past!")
    private LocalDate dueDate;

    @Positive(message = "Estimated duration must be greater than zero!")
    private Integer estimatedDurationMinutes;

    private TaskPriority priority;

    @NotNull(message = "Project ID is required!")
    private Long projectId;

    private Long assignedUserId;
}
