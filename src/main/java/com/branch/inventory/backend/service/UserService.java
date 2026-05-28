package com.branch.inventory.backend.service;

import com.branch.inventory.backend.dto.request.CreateUserRequest;
import com.branch.inventory.backend.dto.request.UpdateProfileRequest;
import com.branch.inventory.backend.dto.request.UpdateUserRequest;
import com.branch.inventory.backend.dto.response.UserResponse;
import com.branch.inventory.backend.model.Branch;
import com.branch.inventory.backend.model.User;
import com.branch.inventory.backend.repository.BranchRepository;
import com.branch.inventory.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final BranchRepository branchRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public UserResponse createUser(CreateUserRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already in use: " + request.getEmail());
        }

        Branch branch = null;
        if (request.getBranchId() != null) {
            branch = branchRepository.findById(request.getBranchId())
                    .orElseThrow(() -> new RuntimeException(
                            "Branch not found: " + request.getBranchId()));
        }

        User user = User.builder()
                .fullName(request.getFullName())
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .role(request.getRole())
                .branch(branch)
                .active(true)
                .build();

        return mapToResponse(userRepository.save(user));
    }

    @Transactional(readOnly = true)
    public Page<UserResponse> getAllUsers(Pageable pageable) {
        return userRepository.findAllWithBranch(pageable).map(this::mapToResponse);
    }

    @Transactional(readOnly = true)
    public UserResponse getUserById(Long id) {
        User user = userRepository.findByIdWithBranch(id)
                .orElseThrow(() -> new RuntimeException("User not found: " + id));
        return mapToResponse(user);
    }

    @Transactional
    public UserResponse updateUser(Long id, UpdateUserRequest request) {
        User user = userRepository.findByIdWithBranch(id)
                .orElseThrow(() -> new RuntimeException("User not found: " + id));

        if (request.getFullName() != null)
            user.setFullName(request.getFullName());
        if (request.getEmail() != null)
            user.setEmail(request.getEmail());
        if (request.getRole() != null)
            user.setRole(request.getRole());

        if (request.getBranchId() != null) {
            Branch branch = branchRepository.findById(request.getBranchId())
                    .orElseThrow(() -> new RuntimeException(
                            "Branch not found: " + request.getBranchId()));
            user.setBranch(branch);
        }

        return mapToResponse(userRepository.save(user));
    }

    @Transactional
    public void deactivateUser(Long id) {
        User user = userRepository.findByIdWithBranch(id)
                .orElseThrow(() -> new RuntimeException("User not found: " + id));
        user.setActive(false);
        userRepository.save(user);
    }

    @Transactional
    public void activateUser(Long id) {
        User user = userRepository.findByIdWithBranch(id)
                .orElseThrow(() -> new RuntimeException("User not found: " + id));
        user.setActive(true);
        userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public UserResponse getByEmail(String email) {
        User user = userRepository.findByEmailWithBranch(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return mapToResponse(user);
    }

    @Transactional
    public UserResponse updateProfile(String email, UpdateProfileRequest request) {
        User user = userRepository.findByEmailWithBranch(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setFullName(request.getName());
        return mapToResponse(userRepository.save(user));
    }

    private UserResponse mapToResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .role(user.getRole().name())
                .branchId(user.getBranch() != null ? user.getBranch().getId() : null)
                .branchName(user.getBranch() != null ? user.getBranch().getName() : null)
                .active(user.isActive())
                .build();
    }
}