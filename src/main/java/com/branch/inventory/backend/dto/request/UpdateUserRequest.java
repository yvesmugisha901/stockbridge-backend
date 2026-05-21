package com.branch.inventory.backend.dto.request;

import com.branch.inventory.backend.model.enums.Role;
import jakarta.validation.constraints.Email;
import lombok.Data;

@Data
public class UpdateUserRequest {

    private String fullName;

    @Email(message = "Invalid email format")
    private String email;

    private Role role;

    private Long branchId;
}