package com.worktime.controller;

import com.worktime.dto.ChangePasswordRequest;
import com.worktime.dto.LoginRequest;
import com.worktime.dto.UserResponse;
import com.worktime.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolderStrategy;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Authentication",
    description = "Operations for user authentication and current user information.")
@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthenticationManager authenticationManager;
    private final SecurityContextRepository securityContextRepository;
    private final SecurityContextHolderStrategy securityContextHolderStrategy;

    private final UserService userService;

    public AuthController(
            AuthenticationManager authenticationManager,
            SecurityContextRepository securityContextRepository,
            UserService userService
    ){
        this.authenticationManager = authenticationManager;
        this.securityContextRepository = securityContextRepository;
        this.securityContextHolderStrategy =
                org.springframework.security.core.context.SecurityContextHolder
                        .getContextHolderStrategy();
        this.userService = userService;
    }

    @GetMapping("/me")
    @Operation(summary = "Get current user",
                description = "Returns the user information associated with the current authenticated session")
    @ApiResponses({
            @ApiResponse(responseCode = "200",
                        description = "Current user retrieved successfully"),
            @ApiResponse(responseCode = "401",
                        description = "Authentication required")
    })
    public UserResponse getCurrentUser(Authentication authentication){
        return userService.getUserByEmail(authentication.getName());
    }


    @PostMapping("/login")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Log in",
            description = "Authenticates the user with email and password and creates an HTTP session")
    @ApiResponses({
            @ApiResponse(responseCode = "200",
                        description = "Login successful"),
            @ApiResponse(responseCode = "400",
                        description = "Invalid login request"),
            @ApiResponse(responseCode = "401",
                        description = "Invalid email or password")
    })
    public void login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse
            ){

        Authentication authenticationRequest =
                UsernamePasswordAuthenticationToken.unauthenticated(
                        request.getEmail(),
                        request.getPassword()
                );
        Authentication authenticationResponse =
                authenticationManager.authenticate(authenticationRequest);

        SecurityContext securityContext =
                securityContextHolderStrategy.createEmptyContext();
        securityContext.setAuthentication(authenticationResponse);

        securityContextHolderStrategy.setContext(securityContext);

        securityContextRepository.saveContext(
                securityContext,
                httpRequest,
                httpResponse
        );
    }

    @PatchMapping("/me/password")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Change current user's password",
                description = "Changes the password of the currently authenticated user")
    @ApiResponses({
            @ApiResponse(responseCode = "204",
                        description = "Password changed successfully"),
            @ApiResponse(responseCode = "400",
                        description = "Invalid password"),
            @ApiResponse(responseCode = "401",
                        description = "Authentication required")
    })
    public void changePassword(
            Authentication authentication,
            @Valid @RequestBody ChangePasswordRequest request
            ){
            userService.changePassword(
                    authentication.getName(),
                    request
            );
    }
}
