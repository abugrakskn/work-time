package com.worktime.dto.timeentry;

import java.time.LocalDate;
import java.util.List;

public record ProjectTimeReportResponse(
        Long projectId,
        String projectName,
        LocalDate periodStart,
        LocalDate periodEnd,
        long totalDurationMinutes,
        List<TimeEntryResponse> timeEntries
) {
}