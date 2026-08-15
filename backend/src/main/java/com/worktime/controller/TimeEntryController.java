package com.worktime.controller;

import com.worktime.dto.timeentry.CreateManualTimeEntryRequest;
import com.worktime.dto.timeentry.StartTimeEntryRequest;
import com.worktime.dto.timeentry.TimeEntryResponse;
import com.worktime.service.TimeEntryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/time-entries")
@RequiredArgsConstructor
public class TimeEntryController {

    private final TimeEntryService timeEntryService;

    @GetMapping
    @Operation(summary = "Get time entries",
                description = "Returns the authenticated user's time entries ordered from newest to oldest.")
    @ApiResponses({
            @ApiResponse(responseCode = "200",
                        description = "Time entries returned successfully"),
            @ApiResponse(responseCode = "401",
                        description = "Authentication required"),
            @ApiResponse(responseCode = "404",
                        description = "User not found")
    })
    public ResponseEntity<List<TimeEntryResponse>> getTimeEntries(Authentication authentication) {
        List<TimeEntryResponse> response =
                timeEntryService.getTimeEntries(authentication.getName());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/active")
    @Operation(summary = "Get the active time entry",
            description = "Returns the authenticated user's active time entry if one exists.")
    @ApiResponses({
            @ApiResponse(responseCode = "200",
                    description = "Active time entry returned successfully"),
            @ApiResponse(responseCode = "204",
                    description = "User does not have an active time entry"),
            @ApiResponse(responseCode = "401",
                    description = "Authentication required"),
            @ApiResponse(responseCode = "404",
                    description = "User not found")
    })
    public ResponseEntity<TimeEntryResponse> getActiveTimeEntry(Authentication authentication) {
        return timeEntryService
                .getActiveTimeEntry(authentication.getName())
                .map(ResponseEntity::ok)
                .orElseGet(() ->
                        ResponseEntity.noContent().build()
                );
    }

    @PostMapping("/start")
    @Operation(summary = "Start a timer",
            description = "Starts a new time entry for the authenticated user and specified task.")
    @ApiResponses({
            @ApiResponse(responseCode = "201",
                        description = "Time entry started successfully"),
            @ApiResponse(responseCode = "400",
                        description = "Invalid request or task status"),
            @ApiResponse(responseCode = "401",
                        description = "Authentication required"),
            @ApiResponse(responseCode = "403",
                        description = "User does not have permission to track time for this task"),
            @ApiResponse(responseCode = "404",
                        description = "User or task not found"),
            @ApiResponse(responseCode = "409",
                        description = "User already has an active time entry")
    })
    public ResponseEntity<TimeEntryResponse> startTimer(
            Authentication authentication,
            @Valid @RequestBody StartTimeEntryRequest request
    ) {
        TimeEntryResponse response = timeEntryService.startTimer(
                authentication.getName(),
                request
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @PostMapping("/stop")
    @Operation(summary = "Stop the active timer",
            description = "Stops the active time entry for the authenticated user.")
    @ApiResponses({
             @ApiResponse(responseCode = "200",
                        description = "Time entry stopped successfully"),
            @ApiResponse(responseCode = "401",
                        description = "Authentication required"),
            @ApiResponse(responseCode = "404",
                        description = "User not found"),
            @ApiResponse(responseCode = "409",
                        description = "User does not have an active time entry")
    })
    public ResponseEntity<TimeEntryResponse> stopTimer(Authentication authentication) {
        TimeEntryResponse timeEntryResponse = timeEntryService.stopTimer(authentication.getName());

        return ResponseEntity.ok(timeEntryResponse);
    }

    @PostMapping("/manual")
    @Operation(summary = "Create a manual time entry",
            description = "Creates a completed time entry and calculates its duration from the supplied start and end times.")
    @ApiResponses({
            @ApiResponse(responseCode = "201",
                        description = "Manual time entry created successfully"),
            @ApiResponse(responseCode = "400",
                        description = "Invalid dates, duration or request"),
            @ApiResponse(responseCode = "401",
                        description = "Authentication required"),
            @ApiResponse(responseCode = "403",
                        description = "User does not have permission to create a time entry for this task"),
            @ApiResponse(responseCode = "404",
                        description = "User or task not found")
    })
    public ResponseEntity<TimeEntryResponse> createManualTimeEntry(
            Authentication authentication,
            @Valid @RequestBody CreateManualTimeEntryRequest request
    ) {
        TimeEntryResponse response =
                timeEntryService.createManualTimeEntry(
                        authentication.getName(),
                        request
                );
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

}
