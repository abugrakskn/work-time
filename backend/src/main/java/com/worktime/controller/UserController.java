package com.worktime.controller;

import com.worktime.dto.UserResponse;
import com.worktime.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "User Management",
        description = "Operations for managing users.")
@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    @Operation(summary = "Get all users",
                description = "Returns all users in the system")
    @ApiResponse(responseCode = "200",
                description = "Users retrieved successfully")
    public List<UserResponse> getAllUsers() {
        return userService.getAllUsers();
    }
}