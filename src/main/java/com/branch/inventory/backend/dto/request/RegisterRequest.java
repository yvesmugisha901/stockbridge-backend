package com.branch.inventory.backend.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * RegisterRequest – payload for POST /api/v1/auth/register
 *
 * Used by the public self-registration form. The resulting user is created
 * with active = false and pendingApproval = true until an admin approves.
 */
@Getter
@Setter
public class RegisterRequest {

    @NotBlank(message = "Full name is required")
    private String fullName;

    @NotBlank(message = "Email is required")
    @Email(message = "Enter a valid email address")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters")
    private String password;

    /**
     * Role the user is requesting.
     * Validated in AuthService — only allow self-selectable roles
     * (not ADMIN, which must be granted by another admin).
     */
    @NotBlank(message = "Role is required")
    private String role;

    @NotNull(message = "Branch ID is required")
    private Long branchId;
}

