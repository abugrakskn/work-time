package com.worktime.dto;

import com.worktime.entity.TaskPriority;
import com.worktime.entity.TaskStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@AllArgsConstructor
public class TaskResponse {
    private Long id;
    private String title;
    private String description;
    private LocalDate dueDate;
    private Integer estimatedDurationMinutes;
    private TaskPriority priority;
    private TaskStatus status;

    private Long projectId;
    private String projectName;

    private Long assignedUserId;
    private String assignedUserName;
}
