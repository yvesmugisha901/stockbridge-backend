package com.branch.inventory.backend.service;

import com.branch.inventory.backend.dto.request.ChangePasswordRequest;
import com.branch.inventory.backend.dto.request.LoginRequest;
import com.branch.inventory.backend.dto.request.RegisterRequest;
import com.branch.inventory.backend.dto.response.AuthResponse;
import com.branch.inventory.backend.model.Branch;
import com.branch.inventory.backend.model.RefreshToken;
import com.branch.inventory.backend.model.User;
import com.branch.inventory.backend.model.enums.Role;
import com.branch.inventory.backend.repository.BranchRepository;
import com.branch.inventory.backend.repository.RefreshTokenRepository;
import com.branch.inventory.backend.repository.UserRepository;
import com.branch.inventory.backend.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;
    private final BranchRepository branchRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserDetailsService userDetailsService;

    @Value("${jwt.refresh-expiration:604800000}")
    private long refreshExpiration;

    // ── Login ─────────────────────────────────────────────────────────────────

    @Transactional
    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()));

        User user = userRepository.findByEmailWithBranch(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        UserDetails userDetails = userDetailsService.loadUserByUsername(request.getEmail());
        String accessToken = jwtUtil.generateToken(userDetails, user);

        return AuthResponse.builder()
                .token(accessToken)
                .email(user.getEmail())
                .fullName(user.getFullName())
                .role(user.getRole().name())
                .branchId(user.getBranch() != null ? user.getBranch().getId() : null)
                .branchName(user.getBranch() != null ? user.getBranch().getName() : null)
                .build();
    }

    // ── Self-registration ─────────────────────────────────────────────────────

    @Transactional
    public void register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already in use: " + request.getEmail());
        }

        // Convert String role to enum safely
        Role role;
        try {
            role = Role.valueOf(request.getRole().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid role: " + request.getRole());
        }

        // Prevent self-registration as ADMIN
        if (role == Role.ADMIN) {
            throw new RuntimeException("Cannot self-register as ADMIN");
        }

        Branch branch = branchRepository.findById(request.getBranchId())
                .orElseThrow(() -> new RuntimeException(
                        "Branch not found: " + request.getBranchId()));

        User user = User.builder()
                .fullName(request.getFullName())
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .role(role) // ← enum, not raw String
                .branch(branch)
                .active(false) // cannot log in until approved
                .pendingApproval(true) // visible in admin's pending list
                .build();

        userRepository.save(user);
    }

    // ── Refresh token creation ────────────────────────────────────────────────

    @Transactional
    public RefreshToken createRefreshToken(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        refreshTokenRepository.revokeAllByUserId(user.getId());

        RefreshToken rt = RefreshToken.builder()
                .token(UUID.randomUUID().toString())
                .user(user)
                .expiresAt(Instant.now().plusMillis(refreshExpiration))
                .revoked(false)
                .build();

        return refreshTokenRepository.save(rt);
    }

    // ── Silent refresh ────────────────────────────────────────────────────────

    @Transactional
    public AuthResponse refreshAccessToken(String refreshTokenValue) {
        RefreshToken stored = refreshTokenRepository.findByToken(refreshTokenValue)
                .orElseThrow(() -> new RuntimeException("Refresh token not found"));

        if (!stored.isValid()) {
            refreshTokenRepository.revokeAllByUserId(stored.getUser().getId());
            throw new RuntimeException("Refresh token expired or revoked. Please log in again.");
        }

        User user = stored.getUser();
        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getEmail());
        String newAccessToken = jwtUtil.generateToken(userDetails, user);

        stored.setRevoked(true);
        refreshTokenRepository.save(stored);

        return AuthResponse.builder()
                .token(newAccessToken)
                .email(user.getEmail())
                .fullName(user.getFullName())
                .role(user.getRole().name())
                .branchId(user.getBranch() != null ? user.getBranch().getId() : null)
                .branchName(user.getBranch() != null ? user.getBranch().getName() : null)
                .build();
    }

    @Transactional
    public RefreshToken rotateRefreshToken(String email) {
        return createRefreshToken(email);
    }

    // ── Logout ────────────────────────────────────────────────────────────────

    @Transactional
    public void logout(String refreshTokenValue) {
        refreshTokenRepository.findByToken(refreshTokenValue)
                .ifPresent(rt -> refreshTokenRepository.revokeAllByUserId(rt.getUser().getId()));
    }

    // ── Change password ───────────────────────────────────────────────────────

    @Transactional
    public void changePassword(String email, ChangePasswordRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPasswordHash())) {
            throw new RuntimeException("Current password is incorrect");
        }

        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new RuntimeException("New passwords do not match");
        }

        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        refreshTokenRepository.revokeAllByUserId(user.getId());
    }
}
