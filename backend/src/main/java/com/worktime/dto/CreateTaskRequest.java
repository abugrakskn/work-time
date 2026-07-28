package com.worktime.dto;

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

    private String title;
    private String description;
    private LocalDate dueDate;

    private Long projectId;
    private Long assignedUserId;
}
