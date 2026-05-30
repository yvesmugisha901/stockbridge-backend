package com.branch.inventory.backend.controller;

import com.branch.inventory.backend.dto.request.LoginRequest;
import com.branch.inventory.backend.dto.request.ChangePasswordRequest;
import com.branch.inventory.backend.dto.response.AuthResponse;
import com.branch.inventory.backend.dto.response.ApiResponse;
import com.branch.inventory.backend.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/change-password")
    public ResponseEntity<ApiResponse<String>> changePassword(
            @Valid @RequestBody ChangePasswordRequest request,
            @AuthenticationPrincipal UserDetails currentUser) {
        authService.changePassword(currentUser.getUsername(), request);
        return ResponseEntity.ok(ApiResponse.success("Password changed successfully."));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<String>> logout() {
        return ResponseEntity.ok(ApiResponse.success("Logged out successfully."));
    }
}