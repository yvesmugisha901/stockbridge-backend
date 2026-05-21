package com.branch.inventory.backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginRequest {

    @NotBlank(message = "Email is required")
    private String email; // renamed from "username" to match service's getEmail()

    @NotBlank(message = "Password is required")
    private String password;
}