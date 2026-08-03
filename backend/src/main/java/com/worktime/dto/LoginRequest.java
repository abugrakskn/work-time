package com.worktime.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@AllArgsConstructor
@Setter
@NoArgsConstructor
public class LoginRequest {
    @Schema(description = "Email address used for authentication",
            example = "ahmet.keskin@example.com")
    @NotBlank(message = "Email cannot be blank")
    @Email(message = "Email must be valid")
    private String email;

    @Schema(description = "Password used for authentication",
            example = "SecurePassword123")
    @NotBlank(message = "Password cannot be blank")
    private String password;
}
