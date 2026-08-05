package com.worktime.dto.project;

import com.worktime.entity.ProjectStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@AllArgsConstructor
public class ProjectResponse{
    @Schema(description = "Unique identifier of the project",
            example = "1")
    private Long id;

    @Schema(description = "Name of the project",
            example = "WorkTime Backend")
    private String name;

    @Schema(description = "Detailed description of the project",
            example = "Backend development for the WorkTime application")
    private String description;

    @Schema(description = "Start date of the project",
            example = "2026-09-01")
    private LocalDate startDate;

    @Schema(description = "Start date of the project",
            example = "2026-10-11")
    private LocalDate endDate;

    @Schema(description = "Current status of the project",
            example = "PLANNED")
    private ProjectStatus status;
}