package com.worktime.dto.task;

import com.worktime.entity.TaskStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@AllArgsConstructor
@Setter
@NoArgsConstructor
public class UpdateTaskStatusRequest {

    @Schema(description = "New status of the task",
            example = "COMPLETED")
    @NotNull(message = "Status is required")
    private TaskStatus status;
}
