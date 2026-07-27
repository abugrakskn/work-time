package com.worktime.dto;

import com.worktime.entity.Project;
import com.worktime.entity.TaskStatus;
import com.worktime.entity.User;

import java.time.LocalDate;

public class TaskResponse {
    private Long id;
    private String title;
    private String description;
    private LocalDate dueDate;
    private TaskStatus status;

    private Long projectId;
    private String projectName;

    private Long assignedUserId;
    private String assignedUserName;
}
