package com.branch.inventory.backend.controller;

import com.branch.inventory.backend.dto.request.ChangePasswordRequest;
import com.branch.inventory.backend.dto.request.LoginRequest;
import com.branch.inventory.backend.dto.request.RegisterRequest;
import com.branch.inventory.backend.dto.response.ApiResponse;
import com.branch.inventory.backend.dto.response.AuthResponse;
import com.branch.inventory.backend.model.RefreshToken;
import com.branch.inventory.backend.service.AuthService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @Value("${app.cookie.secure:false}")
    private boolean cookieSecure;

    @Value("${jwt.refresh-expiration:604800000}")
    private long refreshExpirationMs;

    // ── Login ─────────────────────────────────────────────────────────────────

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletResponse response) {

        AuthResponse authResponse = authService.login(request);
        RefreshToken refreshToken = authService.createRefreshToken(request.getEmail());
        setRefreshCookie(response, refreshToken.getToken());
        return ResponseEntity.ok(authResponse);
    }

    // ── Self-registration (public — no auth required) ─────────────────────────

    /**
     * POST /api/v1/auth/register
     *
     * Anyone can submit a registration request. The created user is saved with
     * active = false and a pendingApproval flag so the admin sees it in the
     * "Pending Requests" tab. No JWT is issued — the user must wait for admin
     * activation before they can log in.
     *
     * Request body (RegisterRequest):
     * fullName – required
     * email – required, unique
     * password – required, min 8 chars (stored hashed)
     * role – required (one of the allowed self-select roles)
     * branchId – required
     *
     * Response 201:
     * { "success": true, "message": "Registration submitted. Awaiting admin
     * approval." }
     *
     * This endpoint must be explicitly permitted in SecurityConfig
     * (see note below).
     */
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<String>> register(
            @Valid @RequestBody RegisterRequest request) {

        authService.register(request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(
                        "Registration submitted. An administrator will review your request."));
    }

    // ── Silent refresh ────────────────────────────────────────────────────────

    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(
            HttpServletRequest request,
            HttpServletResponse response) {

        String refreshTokenValue = extractRefreshCookie(request);

        if (refreshTokenValue == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("No refresh token"));
        }

        try {
            AuthResponse authResponse = authService.refreshAccessToken(refreshTokenValue);
            RefreshToken newRefreshToken = authService.rotateRefreshToken(authResponse.getEmail());
            setRefreshCookie(response, newRefreshToken.getToken());
            return ResponseEntity.ok(authResponse);
        } catch (RuntimeException e) {
            clearRefreshCookie(response);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    // ── Logout ────────────────────────────────────────────────────────────────

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<String>> logout(
            HttpServletRequest request,
            HttpServletResponse response) {

        String refreshTokenValue = extractRefreshCookie(request);
        if (refreshTokenValue != null) {
            authService.logout(refreshTokenValue);
        }
        clearRefreshCookie(response);
        return ResponseEntity.ok(ApiResponse.success("Logged out successfully."));
    }

    // ── Change password ───────────────────────────────────────────────────────

    @PostMapping("/change-password")
    public ResponseEntity<ApiResponse<String>> changePassword(
            @Valid @RequestBody ChangePasswordRequest request,
            @AuthenticationPrincipal UserDetails currentUser,
            HttpServletResponse response) {

        authService.changePassword(currentUser.getUsername(), request);
        clearRefreshCookie(response);
        return ResponseEntity.ok(ApiResponse.success("Password changed successfully."));
    }

    // ── Cookie helpers ────────────────────────────────────────────────────────

    // NOTE: SameSite changed from "Strict" to "Lax". The frontend (localhost:3000)
    // and backend (localhost:8080) are different origins, so this cookie is sent
    // cross-site on every fetch from the Next.js app. "Strict" cookies are not
    // attached to cross-site requests at all (and some Chromium versions won't
    // even persist a "Strict" cookie received from a cross-site response), which
    // is why refresh_token never appeared in the cookie jar. "Lax" allows the
    // cookie to be stored and sent in this same-app cross-port dev setup while
    // still blocking it for genuine third-party/tracking contexts.
    private void setRefreshCookie(HttpServletResponse response, String token) {
        response.addHeader("Set-Cookie",
                String.format("refresh_token=%s; Path=/api/v1/auth; HttpOnly; %sSameSite=Lax; Max-Age=%d",
                        token,
                        cookieSecure ? "Secure; " : "",
                        refreshExpirationMs / 1000));
    }

    private void clearRefreshCookie(HttpServletResponse response) {
        response.addHeader("Set-Cookie",
                "refresh_token=; Path=/api/v1/auth; HttpOnly; SameSite=Lax; Max-Age=0");
    }

    private String extractRefreshCookie(HttpServletRequest request) {
        if (request.getCookies() == null)
            return null;
        return Arrays.stream(request.getCookies())
                .filter(c -> "refresh_token".equals(c.getName()))
                .map(Cookie::getValue)
                .findFirst()
                .orElse(null);
    }
}
