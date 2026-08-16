package com.worktime.dto.timeentry;

import java.time.LocalDate;

public record TimeSummaryResponse(
        LocalDate periodStart,
        LocalDate periodEnd,
        long totalDurationMinutes
) {}
