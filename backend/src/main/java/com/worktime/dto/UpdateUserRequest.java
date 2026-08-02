package com.worktime.dto;

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
public class UpdateUserRequest {
    @Schema(description = "Updated first name of the user",
            example = "Ahmet Buğra")
    @Size(max = 50, message = "First name cannot exceed 50 characters")
    private String firstName;

    @Schema(description = "Updated last name of the user",
            example = "Keskin")
    @Size(max = 50, message = "Last name cannot exceed 50 characters")
    private String lastName;

    @Schema(description = "Updated email address used for authentication",
            example = "abugra.keskin@example.com")
    @Email(message = "Email must be valid")
    private String email;

    @Schema(description = "Updated role assigned to the user",
            example = "ADMIN")
    private UserRole role;

    @Schema(description = "Updated account status of the user",
            example = "false")
    private Boolean active;
}
