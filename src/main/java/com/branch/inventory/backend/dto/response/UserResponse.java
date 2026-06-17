package com.branch.inventory.backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {

    private Long id;
    private String email;
    private String fullName;
    private String role;
    private Long branchId;
    private String branchName;
    private boolean active;
    private boolean pendingApproval; // ← added
    private LocalDateTime createdAt;
}
