package com.worktime.dto.timeentry;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class StartTimeEntryRequest {

    @Schema(description = "ID of the task to start tracking",
            example = "1")
    @NotNull(message = "Task ID is required")
    private Long taskId;

    @Schema(description = "Optional description of the work",
            example = "Implementing the authentication flow")
    @Size(max = 1000,
            message = "Description cannot exceed 1000 characters")
    private String description;
}