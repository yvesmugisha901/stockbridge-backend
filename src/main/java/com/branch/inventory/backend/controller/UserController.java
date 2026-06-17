package com.branch.inventory.backend.controller;

import com.branch.inventory.backend.dto.request.CreateUserRequest;
import com.branch.inventory.backend.dto.request.UpdateProfileRequest;
import com.branch.inventory.backend.dto.request.UpdateUserRequest;
import com.branch.inventory.backend.dto.response.ApiResponse;
import com.branch.inventory.backend.dto.response.UserResponse;
import com.branch.inventory.backend.service.EmailService;
import com.branch.inventory.backend.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

import java.util.List;

/**
 * UserController – manages user accounts.
 * FR-01: User Registration (Admin only)
 * FR-04: Password Management
 * FR-05: User Deactivation
 * NFR-07: Layered architecture enforced
 *
 * IMPORTANT – route ordering:
 * Literal paths (/me, /pending) MUST be declared before /{id} so Spring
 * does not attempt to parse "me" or "pending" as a Long.
 */
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final EmailService emailService;

    // ── Literal sub-paths first (before any /{id} mapping) ───────────────────

    /**
     * GET /api/v1/users/me
     * Returns the profile of the currently authenticated user.
     */
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserResponse>> getMe(
            @AuthenticationPrincipal UserDetails userDetails) {
        UserResponse user = userService.getByEmail(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success(user));
    }

    /**
     * PUT /api/v1/users/me
     * Updates the profile of the currently authenticated user.
     */
    @PutMapping("/me")
    public ResponseEntity<ApiResponse<UserResponse>> updateMe(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody UpdateProfileRequest request) {
        UserResponse updated = userService.updateProfile(userDetails.getUsername(), request);
        return ResponseEntity.ok(ApiResponse.success(updated));
    }

    /**
     * GET /api/v1/users/pending
     * Returns all self-registered users who have not yet been activated.
     * Admin only.
     *
     * Must live ABOVE /{id} — otherwise Spring tries to cast "pending" → Long
     * and throws MethodArgumentTypeMismatchException (HTTP 500).
     */
    @GetMapping("/pending")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<UserResponse>>> getPendingUsers() {
        List<UserResponse> pending = userService.getPendingUsers();
        return ResponseEntity.ok(ApiResponse.success(pending));
    }

    // ── Collection endpoints ──────────────────────────────────────────────────

    /**
     * POST /api/v1/users
     * Admin creates a new user account with role and branch assignment.
     * A welcome email with login credentials is sent asynchronously.
     * FR-01
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<UserResponse>> createUser(
            @Valid @RequestBody CreateUserRequest request) {

        UserResponse user = userService.createUser(request);

        emailService.sendWelcomeEmail(
                user.getFullName(),
                user.getEmail(),
                request.getPassword(),
                user.getRole(),
                user.getBranchName());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(user));
    }

    /**
     * GET /api/v1/users
     * Returns a paginated list of all users. Admin only.
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Page<UserResponse>>> getAllUsers(Pageable pageable) {
        Page<UserResponse> users = userService.getAllUsers(pageable);
        return ResponseEntity.ok(ApiResponse.success(users));
    }

    // ── Single-resource endpoints (/{id} last) ────────────────────────────────

    /**
     * GET /api/v1/users/{id}
     * Returns details of a specific user. Admin only.
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<UserResponse>> getUserById(@PathVariable Long id) {
        UserResponse user = userService.getUserById(id);
        return ResponseEntity.ok(ApiResponse.success(user));
    }

    /**
     * PUT /api/v1/users/{id}
     * Updates user details (name, email, role, branch). Admin only.
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<UserResponse>> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UpdateUserRequest request) {
        UserResponse updated = userService.updateUser(id, request);
        return ResponseEntity.ok(ApiResponse.success(updated));
    }

    /**
     * DELETE /api/v1/users/{id}
     * Permanently deletes a user account. Admin only.
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<String>> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.ok(ApiResponse.success("User deleted successfully."));
    }

    /**
     * DELETE /api/v1/users/{id}/reject
     * Rejects and permanently removes a pending self-registration request.
     * Optionally accepts a reason in the request body (for future email
     * notification).
     * Admin only.
     */
    @DeleteMapping("/{id}/reject")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<String>> rejectUser(
            @PathVariable Long id,
            @RequestBody(required = false) RejectRequest body) {
        userService.rejectPendingUser(id, body != null ? body.getReason() : null);
        return ResponseEntity.ok(ApiResponse.success("Registration request rejected."));
    }

    /**
     * PATCH /api/v1/users/{id}/deactivate
     * Deactivates a user account without deleting it. FR-05
     */
    @PatchMapping("/{id}/deactivate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<String>> deactivateUser(@PathVariable Long id) {
        userService.deactivateUser(id);
        return ResponseEntity.ok(ApiResponse.success("User deactivated successfully."));
    }

    /**
     * PATCH /api/v1/users/{id}/activate
     * Activates a user account (works for both re-activation and first-time
     * approval of a self-registered pending user).
     */
    @PatchMapping("/{id}/activate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<String>> activateUser(@PathVariable Long id) {
        userService.activateUser(id);
        return ResponseEntity.ok(ApiResponse.success("User activated successfully."));
    }

    // ── Inner DTO for reject body ─────────────────────────────────────────────

    /**
     * Simple inline DTO for the optional rejection reason.
     * Move to its own file under dto/request/ if you prefer.
     */
    public static class RejectRequest {
        private String reason;

        public String getReason() {
            return reason;
        }

        public void setReason(String reason) {
            this.reason = reason;
        }
    }
}
