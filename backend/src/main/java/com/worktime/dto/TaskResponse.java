package com.worktime.dto;

import com.worktime.entity.TaskPriority;
import com.worktime.entity.TaskStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@AllArgsConstructor
public class TaskResponse {
    @Schema(description = "Unique identifier of the task",
            example = "1")
    private Long id;

    @Schema(description = "Title of the task",
            example = "Implement JWT authentication")
    private String title;

    @Schema(description = "Detailed description of the task",
            example = "Implement JWT authentication and configure Spring Security")
    private String description;

    @Schema(description = "Due date of the task",
            example = "2027-09-01")
    private LocalDate dueDate;

    @Schema(description = "Estimated duration of the task in minutes",
            example = "180")
    private Integer estimatedDurationMinutes;

    @Schema(description = "Priority level of the task",
            example = "HIGH")
    private TaskPriority priority;

    @Schema(description = "Current status of the project",
            example = "IN_PROGRESS")
    private TaskStatus status;

    @Schema(description = "ID of the project that the task belongs to",
            example = "1")
    private Long projectId;

    @Schema(description = "Name of the project that the task belongs to",
            example = "WorkTime Backend")
    private String projectName;

    @Schema(description = "ID of the user assigned to the task",
            example = "1",
            nullable = true)
    private Long assignedUserId;

    @Schema(description = "Name of the user assigned to the task",
            example = "Ahmet Buğra Keskin",
            nullable = true)
    private String assignedUserName;
}
