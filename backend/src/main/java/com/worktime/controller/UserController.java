package com.worktime.controller;

import com.worktime.dto.ChangePasswordRequest;
import com.worktime.dto.CreateUserRequest;
import com.worktime.dto.UpdateUserRequest;
import com.worktime.dto.UserResponse;
import com.worktime.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

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

    @GetMapping("/{id}")
    @Operation(summary = "Get user by ID",
                description = "Returns the user with the specified ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200",
                        description = "User retrieved successfully"),
            @ApiResponse(responseCode = "404",
                        description = "User not found")
    })
    public UserResponse getUserById(@PathVariable Long id){
        return userService.getUserById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a new user",
                description = "Creates a new user")
    @ApiResponses({
            @ApiResponse(responseCode = "201",
                    description = "User created"),
            @ApiResponse(responseCode = "400",
                    description = "Invalid user data"),
            @ApiResponse(responseCode = "409",
                    description = "Email is already in use")
    })
    public UserResponse createUser(@Valid @RequestBody CreateUserRequest request){
        return userService.createUser(request);
    }

    @PatchMapping("/{id}")
    @Operation(summary = "Update the user information",
                description = "Updates the user information")
    @ApiResponses({
            @ApiResponse(responseCode = "200",
                    description = "User updated successfully"),
            @ApiResponse(responseCode = "400",
                    description = "Invalid user data"),
            @ApiResponse(responseCode = "404",
                    description = "User not found"),
            @ApiResponse(responseCode = "409",
                    description = "Email is already in use")
    })
    public UserResponse patchUser(@PathVariable Long id,@Valid @RequestBody UpdateUserRequest request){
        return userService.patchUser(id, request);
    }

}