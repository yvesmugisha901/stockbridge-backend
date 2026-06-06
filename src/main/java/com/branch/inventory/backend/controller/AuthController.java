package com.branch.inventory.backend.controller;

import com.branch.inventory.backend.dto.request.ChangePasswordRequest;
import com.branch.inventory.backend.dto.request.LoginRequest;
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
    private boolean cookieSecure; // set true in production

    @Value("${jwt.refresh-expiration:604800000}")
    private long refreshExpirationMs;

    // ── Login ─────────────────────────────────────────────────────────────────

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletResponse response) {

        // 1. Authenticate and get access token
        AuthResponse authResponse = authService.login(request);

        // 2. Create a refresh token and set it as httpOnly cookie
        RefreshToken refreshToken = authService.createRefreshToken(request.getEmail());
        setRefreshCookie(response, refreshToken.getToken());

        return ResponseEntity.ok(authResponse);
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
            // Validate old token, issue new access token
            AuthResponse authResponse = authService.refreshAccessToken(refreshTokenValue);

            // Rotate: issue a new refresh token cookie
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

        // Clear refresh cookie — forces re-login after password change
        clearRefreshCookie(response);

        return ResponseEntity.ok(ApiResponse.success("Password changed successfully."));
    }

    // ── Cookie helpers ────────────────────────────────────────────────────────

    private void setRefreshCookie(HttpServletResponse response, String token) {
        Cookie cookie = new Cookie("refresh_token", token);
        cookie.setHttpOnly(true);
        cookie.setSecure(cookieSecure);
        cookie.setPath("/api/v1/auth"); // only sent to auth endpoints
        cookie.setMaxAge((int) (refreshExpirationMs / 1000));
        // SameSite=Strict via header (Cookie API doesn't expose it pre-Servlet 6)
        response.addHeader("Set-Cookie",
                String.format("refresh_token=%s; Path=/api/v1/auth; HttpOnly; %sSameSite=Strict; Max-Age=%d",
                        token,
                        cookieSecure ? "Secure; " : "",
                        refreshExpirationMs / 1000));
    }

    private void clearRefreshCookie(HttpServletResponse response) {
        response.addHeader("Set-Cookie",
                "refresh_token=; Path=/api/v1/auth; HttpOnly; SameSite=Strict; Max-Age=0");
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
