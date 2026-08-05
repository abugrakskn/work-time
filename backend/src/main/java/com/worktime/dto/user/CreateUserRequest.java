package com.worktime.dto.user;

import com.worktime.entity.UserRole;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@AllArgsConstructor
@Setter
@NoArgsConstructor
public class CreateUserRequest {
    @Schema(description = "First name of the user",
            example = "Ahmet")
    @NotBlank(message = "First name cannot be blank")
    @Size(max = 50, message = "First name cannot exceed 50 characters")
    private String firstName;

    @Schema(description = "Last name of the user",
            example = "Keskin")
    @NotBlank(message = "Last name cannot be blank")
    @Size(max = 50, message = "Last name cannot exceed 50 characters")
    private String lastName;

    @Schema(description = "Email address used for authentication",
            example = "ahmet.keskin@example.com")
    @NotBlank(message = "Email cannot be blank")
    @Email(message = "Email must be valid")
    private String email;

    @Schema(description = "Password used to authenticate the user",
            example = "SecurePassword123!")
    @NotBlank(message = "Password cannot be blank")
    @Size(min = 8, message = "Password must be at least 8 characters")
    private String password;

    @Schema(description = "Role assigned to the user",
            example = "ADMIN")
    @NotNull(message = "User role is required")
    private UserRole role;
}
