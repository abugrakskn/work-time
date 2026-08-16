package com.worktime.dto.timeentry;

import java.time.LocalDate;
import java.util.List;

public record UserTimeReportResponse(
        Long userId,
        String userName,
        LocalDate periodStart,
        LocalDate periodEnd,
        long totalDurationMinutes,
        List<TimeEntryResponse> timeEntries
) {
}