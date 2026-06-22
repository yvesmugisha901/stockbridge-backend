package com.branch.inventory.backend.dto.response;

import lombok.*;

/**
 * Returned by POST /api/v1/auth/login.
 *
 * The access token is short-lived (15 min).
 * The refresh token is NOT in this body — it is set as an httpOnly cookie
 * by AuthController before returning this response.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {

    private String token; // short-lived JWT access token
    private String email;
    private String fullName;
    private String role;
    private Long branchId;
    private String branchName;
}
