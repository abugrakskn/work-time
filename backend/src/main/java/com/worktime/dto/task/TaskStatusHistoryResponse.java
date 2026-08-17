package com.worktime.dto.task;

import com.worktime.entity.TaskStatus;

import java.time.LocalDateTime;

public record TaskStatusHistoryResponse(
        Long id,
        TaskStatus previousStatus,
        TaskStatus newStatus,
        Long changedByUserId,
        String changedByUserName,
        LocalDateTime changedAt
) {
}