package com.branch.inventory.backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {

    private String token;
    private String email; // renamed from "username" to match service's .email()
    private String fullName;
    private String role; // changed from Role enum to String to match service's
                         // .role(user.getRole().name())
    private Long branchId;
    private String branchName;
}