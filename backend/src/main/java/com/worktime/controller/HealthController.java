package com.worktime.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Health check",
        description = "Endpoints for monitoring the application health.")
@RestController
public class HealthController{

    @GetMapping("/health")
    @Operation(summary = "Check application health",
            description = "Checks whether the application is running.")
    @ApiResponse(responseCode = "200",
            description = "Application is running")
    public String health(){
        return "Application is running.";
    }

}