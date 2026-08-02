package com.worktime.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@AllArgsConstructor
@Setter
@NoArgsConstructor
public class ChangePasswordRequest {
    @Schema(description = "Current password of the authenticated user",
            example = "OldPassword123!")
    @NotBlank(message = "Current password cannot be blank")
    private String currentPassword;

    @Schema(description = "New password to be assigned to the user",
            example = "NewSecurePassword123!")
    @NotBlank(message = "New password cannot be blank")
    @Size(min = 8, max = 100,
            message = "New password must be between 8 and 100 characters")
    private String newPassword;
}
