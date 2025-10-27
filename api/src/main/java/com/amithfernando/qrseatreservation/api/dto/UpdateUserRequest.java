package com.amithfernando.qrseatreservation.api.dto;

import com.amithfernando.qrseatreservation.api.enums.Role;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request object for updating user information")
public class UpdateUserRequest {

    @Schema(description = "New username (optional)", example = "admin_new")
    @Size(min = 3, max = 64, message = "Username must be between 3 and 64 characters")
    private String username;

    @Schema(description = "New password (optional, will be encrypted)", example = "NewSecurePass123!")
    @Size(min = 6, max = 100, message = "Password must be between 6 and 100 characters")
    private String password;

    @Schema(description = "New role (optional)", example = "ADMIN")
    private Role role;

    @Schema(description = "Whether the user account is enabled (optional)", example = "true")
    private Boolean enabled;
}
