package com.worktime.dto;

import com.worktime.entity.UserRole;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UserResponse {

    @Schema(description = "Unique identifier of the user",
            example = "1")
    private Long id;

    @Schema(description = "First name of the user",
            example = "Ahmet")
    private String firstName;

    @Schema(description = "Last name of the user",
            example = "Keskin")
    private String lastName;

    @Schema(description = "Email of the user",
            example = "ahmet.keskin@example.com")
    private String email;

    @Schema(description = "Role assigned to the user",
            example = "ADMIN")
    private UserRole role;

    @Schema(description = "Indicates whether the user account is active",
            example = "true")
    private boolean active;
}