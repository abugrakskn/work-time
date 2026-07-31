package com.worktime.dto;

import com.worktime.entity.ProjectStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreateProjectRequest {

    @Schema(description = "Name of the project",
            example = "WorkTime Backend")
    @NotBlank(message = "Project name cannot be blank")
    @Size(max = 100, message = "Project name cannot exceed 100 characters")
    private String name;

    @Schema(description = "Description of the project",
            example = "Backend development for the WorkTime application")
    @Size(max = 1000, message = "Description cannot exceed 1000 characters")
    private String description;

    @Schema(description = "Planned start date of the project",
            example = "2026-08-01")
    private LocalDate startDate;

    @Schema(description = "Planned end date of the project",
            example = "2026-09-30")
    private LocalDate endDate;

    @Schema(description = "Current status of the project",
            example = "ACTIVE")
    @NotNull(message = "Project status is required")
    private ProjectStatus status;
}
