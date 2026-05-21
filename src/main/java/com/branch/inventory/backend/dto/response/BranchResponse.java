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
public class BranchResponse {

    private Long id;
    private String name;
    private String code;
    private String location;
    private String contactInfo; // renamed from "contactPhone" to match service's .contactInfo()
    private boolean active;
    private LocalDateTime createdAt;
}