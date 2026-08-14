package com.worktime.dto.timeentry;

import java.time.LocalDateTime;

public record TimeEntryResponse(
        Long id,
        Long userId,
        String userName,
        Long taskId,
        String taskTitle,
        LocalDateTime startTime,
        LocalDateTime endTime,
        Integer durationMinutes,
        String description
) {
}